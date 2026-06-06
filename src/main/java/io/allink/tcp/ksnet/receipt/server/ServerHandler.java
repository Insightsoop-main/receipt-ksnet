package io.allink.tcp.ksnet.receipt.server;

import static io.allink.tcp.ksnet.receipt.common.Code.CANCEL_TYPE_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.CARD_COMPANY_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.CHECK_YN_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.DDC_YN_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.FOREIGN_YN_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.PAY_TYPE_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.SVC_TYPE_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.SWIPE_MAP;
import static io.allink.tcp.ksnet.receipt.common.Code.TRD_TYPE_MAP;

import io.allink.tcp.ksnet.receipt.model.Store;
import io.allink.tcp.ksnet.receipt.protocol.KsnetMessage;
import io.allink.tcp.ksnet.receipt.service.MerchantReceiptService;
import io.allink.tcp.ksnet.receipt.service.StoreService;
import io.allink.tcp.ksnet.receipt.util.JsonUtil;
import io.allink.tcp.ksnet.receipt.util.NormalizedPayloadBuilder;
import io.allink.tcp.ksnet.receipt.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ServerHandler extends ChannelInboundHandlerAdapter {
  private static final Charset EUC_KR = Charset.forName("EUC-KR"); // EUC-KR 인코딩 선언

  private KsnetMessage receipt;

  private final StoreService storeService;

  private final MerchantReceiptService mertReceiptService;

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    // 버퍼 할당 제거 (메모리 누수 수정)
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    String remoteAddress = ctx.channel().remoteAddress().toString();
    log.info("channel active: {}", remoteAddress);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    String serverId = ctx.channel().id().asShortText();
    log.info("Client disconnected: " + ctx.channel().remoteAddress());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object message) {
    receipt = (KsnetMessage) message;
    log.info("Received message: {}", receipt);

    // 가맹점 등록 여부와 관계없이 항상 성공 응답
    String resCd = "00";

    String trxId = (receipt.getTransDate() + "-" + receipt.getTrdUniKey()).trim();

    if (mertReceiptService.isExists(trxId)) {
      // 중복 요청은 DB 저장 생략, 응답은 성공으로
      log.info("중복 전문 수신 trxId: {}", trxId);
    } else {
      try {
        Store store =
            storeService.findAllByBusinessNoAndDeviceId(receipt.getBusinessNo(), receipt.getTermId());

        if (store == null) {
          log.warn("미등록 가맹점 수신 - DB 적재: businessNo={}, terminalId={}",
              receipt.getBusinessNo(), receipt.getTermId());
        }

        String svcType = Objects.toString(receipt.getSvcType(), "");
        KsnetMessage ksMsg = (KsnetMessage) receipt.clone();

        ksMsg.setTrdType(TRD_TYPE_MAP.getOrDefault(svcType, svcType));
        ksMsg.setCardNo(StringUtil.maskCardNumber(ksMsg.getCardNo()));
        ksMsg.setSvcType(SVC_TYPE_MAP.getOrDefault(svcType, svcType));
        ksMsg.setInsMon(ksMsg.getInsMon().equals("00") ? "일시불" : ksMsg.getInsMon());
        ksMsg.setIssCd(CARD_COMPANY_MAP.getOrDefault(ksMsg.getIssCd(), ksMsg.getIssCd()));
        ksMsg.setBuyCd(CARD_COMPANY_MAP.getOrDefault(ksMsg.getBuyCd(), ksMsg.getBuyCd()));
        ksMsg.setCheckYn("C".equals(ksMsg.getCheckYn()) ? "체크카드" : "");
        ksMsg.setSwipe(SWIPE_MAP.getOrDefault(ksMsg.getSwipe(), ksMsg.getSwipe()));

        // mchNo는 KSNET 원본 가맹점번호 그대로 저장 (store_uid로 변경하지 않음)

        String normalizedPayload = NormalizedPayloadBuilder.build(store, ksMsg);
        mertReceiptService.insertWithJson(ksMsg, JsonUtil.toJson(store, ksMsg), normalizedPayload);

      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    // 응답 발송
    String res = generateResponse(resCd, receipt);
    ByteBuf reqBuf = Unpooled.copiedBuffer(res, EUC_KR);
    log.info("Server response: {}", res);
    ctx.writeAndFlush(reqBuf).addListener(ChannelFutureListener.CLOSE);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // 실패 응답
    ByteBuf reqBuf = Unpooled.copiedBuffer(generateResponse("99", receipt), EUC_KR);
    ctx.writeAndFlush(reqBuf).addListener(ChannelFutureListener.CLOSE);

    // Close the connection when an exception is raised.
    ctx.close();
    log.error("exception caught: {}", cause.getMessage());
  }

  private String generateResponse(String resCd, KsnetMessage msg) {
    LocalDateTime transDateTime =
        LocalDateTime.parse(
            msg.getTransDate() + msg.getTransTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    String response =
        new StringBuilder()
            .append((char) 0x02) // STX
            .append("RK") // 거래구분 (2)
            .append(StringUtil.formatFixedLength(msg.getSvcType(), 2)) // 승인/취소 구분 (2)
            .append(resCd) // 송수신응답코드 (2)
            .append(StringUtil.formatFixedLength(msg.getTermId(), 10)) // 단말기번호 (10)
            .append(
                StringUtil.formatFixedLength(
                    transDateTime.format(DateTimeFormatter.ofPattern("yyMMddHHmmss")),
                    12)) //  송신일시 (12)
            .append(StringUtil.formatFixedLength(msg.getApprovalDate(), 12)) // 승인일시 (12)
            .append(StringUtil.formatFixedLength(msg.getAuDate(), 12)) // 원승인일시 (12)
            .append(StringUtil.formatFixedLength(msg.getTrdUniKey(), 12)) // 거래고유번호 (12)
            .append(StringUtil.formatFixedLength(msg.getSwipe(), 1)) // Pos Entry Mode (1)
            .append(StringUtil.formatFixedLength(msg.getCardNo(), 20)) // 카드번호 (20)
            .append(StringUtil.formatFixedLength(msg.getExpiryDate(), 4)) // 유효기간 (4)
            .append(StringUtil.formatFixedLength(msg.getInsMon(), 2)) // 할부개월수 (2)
            .append(StringUtil.formatFixedLength(msg.getTrdAmtTot(), 12)) // 총금액 (12)
            .append(StringUtil.formatFixedLength(msg.getSvcAmt(), 12)) // 봉사료 (12)
            .append(StringUtil.formatFixedLength(msg.getTaxAmt(), 12)) // 부가세 (12)
            .append(StringUtil.formatFixedLength(msg.getAuNo(), 13)) // 승인번호 (13)
            .append(StringUtil.formatFixedLength(msg.getOriTrdUniKey(), 13)) // 원거래승인번호 (13)
            .append(StringUtil.formatFixedLength(msg.getCardTypeNm(), 16)) // 카드종류명 (16)
            .append(StringUtil.formatFixedLength(msg.getIssCd(), 2)) // 발급사코드 (2)
            .append(StringUtil.formatFixedLength(msg.getBuyCd(), 2)) // 매입사코드 (2)
            .append(StringUtil.formatFixedLength(msg.getBusinessNo(), 10)) // 사업자번호 (10)
            .append(StringUtil.formatFixedLength(msg.getMchNo(), 15)) // 가맹점번호 (15)
            .append(StringUtil.formatFixedLength(msg.getDdcYn(), 1)) // 매입구분 (1)
            .append(StringUtil.formatFixedLength(msg.getCheckYn(), 1)) // 카드구분자 (1)
            .append(StringUtil.formatFixedLength(msg.getIcFeeRate(), 4)) // 현금 IC 가맹점수수료율 (4)
            .append(StringUtil.formatFixedLength(msg.getIcFee(), 12)) // 현금 IC 가맹점수수료 (12)
            .append(StringUtil.formatFixedLength(msg.getIcIssuerBankCd(), 3)) // 현금 IC 발급은행코드 (3)
            .append(StringUtil.formatFixedLength(msg.getIcAcquireBankCd(), 3)) // 현금 IC 매입은행코드 (3)
            .append(StringUtil.formatFixedLength(msg.getUsageInfo(), 50)) // 업체사용정보 (50)
            .append(StringUtil.formatFixedLength(msg.getFiller(), 25)) // filer (25)
            .append((char) 0x03) // ETX
            .append((char) 0x0D) // CR
            .toString();

//    try {
//      // EUC-KR로 인코딩
//      byte[] eucKrBytes = response.getBytes("EUC-KR");
//      return new String(eucKrBytes, "EUC-KR");
//    } catch (UnsupportedEncodingException e) {
//      throw new RuntimeException(e);
//    }

    return response;
  }
}
