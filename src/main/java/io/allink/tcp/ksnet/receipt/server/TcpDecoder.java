package io.allink.tcp.ksnet.receipt.server;

import io.allink.tcp.ksnet.receipt.protocol.KsnetMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TcpDecoder extends ByteToMessageDecoder {

  private static final Charset EUC_KR = Charset.forName("EUC-KR"); // EUC-KR 인코딩 선언

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    // 1. STX(0x02) 확인
    if (in.readableBytes() < 1 || in.readByte() != 0x02) {
      ctx.close();
      return;
    }

    // 2. ETX(0x03) 위치 탐색
    int etxPos = in.indexOf(in.readerIndex(), in.writerIndex(), (byte) 0x03);
    if (etxPos == -1) {
      return; // ETX 미도착 → 버퍼에 계속 축적
    }

    // 3. 데이터 추출 (STX 이후 ~ ETX 이전)
    byte[] data = new byte[etxPos - in.readerIndex()];
    in.readBytes(data);

    log.info("Received KSNET data: {}", new String(data, EUC_KR));

    ByteBuf buf = Unpooled.buffer(data.length);
    buf.writeBytes(data);

    String transactionType = readString(buf, 2); // 거래구분 (2)
    String svcType = readString(buf, 2); // 승인/취소 구분 (2)
    String trdType = readString(buf, 2); // 업무구분/송수신응답코드 (2)

    String termId = readString(buf, 10); // 단말기번호 (10)

    String transDtStr = readString(buf, 12); // 송신일시 (12) - yyMMddHHmmss 형식
    LocalDateTime transDateTime =
        LocalDateTime.parse(transDtStr, DateTimeFormatter.ofPattern("yyMMddHHmmss"));

    String transDate = transDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String transTime = transDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));

    String approvalDate = readString(buf, 12); // 승인일시 (12)
    String auDate = readString(buf, 12); // 원승인일시 (12)

    String trdUniKey = readString(buf, 12); // 거래고유번호 (12)
    String swipe = readString(buf, 1); // Pos Entry Mode (1)
    String cardNo = readString(buf, 20); // 카드번호 (20)
    String expiryDate = readString(buf, 4); // 유효기간 (4)
    String insMon = readString(buf, 2); // 할부개월수 (2)
    String trdAmtTot = readString(buf, 12); // 총금액 (12)
    String svcAmt = readString(buf, 12); // 봉사료 (12)
    String taxAmt = readString(buf, 12); // 부가세 (12)
    String auNo = readString(buf, 13); // 승인번호 (13)
    String oriTrdUniKey = readString(buf, 13); // 원거래승인번호 (13)
    String cardTypeNm = readString(buf, 16); // 카드종류명 (16)
    String issCd = readString(buf, 2); // 발급사코드 (2)
    String buyCd = readString(buf, 2); // 매입사코드 (2)
    String businessNo = readString(buf, 10); // 사업자번호 (10)
    String mchNo = readString(buf, 15); // 가맹점번호 (15)
    String ddcYn = readString(buf, 1); // 매입구분 (1)
    String checkYn = readString(buf, 1); // 카드구분자 (1)
    String icFeeRate = readString(buf, 4); // 현금 IC 가맹점수수료율 (4)
    String icFee = readString(buf, 12); // 현금 IC 가맹점수수료 (12)
    String icIssuerBankCd = readString(buf, 3); // 현금 IC 발급은행코드 (3)
    String icAcquireBankCd = readString(buf, 3); // 현금 IC 매입은행코드 (3)
    String usageInfo = readString(buf, 50); // 업체사용정보 (50)
    String filler = readString(buf, 25); // filler (50)

    KsnetMessage receipt =
        new KsnetMessage(
            transactionType,
            svcType,
            trdType,
            termId,
            transDate,
            transTime,
            approvalDate,
            auDate,
            trdUniKey,
            swipe,
            cardNo,
            expiryDate,
            insMon,
            trdAmtTot,
            svcAmt,
            taxAmt,
            auNo,
            oriTrdUniKey,
            cardTypeNm,
            issCd,
            buyCd,
            businessNo,
            mchNo,
            ddcYn,
            checkYn,
            icFeeRate,
            icFee,
            icIssuerBankCd,
            icAcquireBankCd,
            usageInfo,
            filler);

    log.info("Decoded KSNET data: {}", receipt);

    // ServerHandler 에 전달
    out.add(receipt);
  }

  private String readString(ByteBuf in, int length) {
    byte[] bytes = new byte[length];
    in.readBytes(bytes);
    return new String(bytes, EUC_KR).trim();
  }
}
