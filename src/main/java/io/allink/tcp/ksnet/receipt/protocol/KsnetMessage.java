package io.allink.tcp.ksnet.receipt.protocol;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KsnetMessage implements Cloneable {
  private String transactionType; // (NEW) 거래구분
  private String svcType; // 승인/취소 구분
  private String trdType; // 업무구분/송수신응답코드
  private String termId; // Terminal ID
  private String transDate; // 전송일자 YYYYMMDD
  private String transTime; // 전송시간 hh24miss
  private String approvalDate; // (NEW) 승인일시 YYMMDDhhmmss
  private String auDate; // 원승인일시
  private String trdUniKey; // 거래고유번호
  private String swipe; // Swipe 구분 - S:Swipe, K:key-in
  private String cardNo; // 카드번호
  private String expiryDate; // (NEW) 유효기간 - 전송불가 (실제론 빈값)
  private String insMon; // 할부개월수
  private String trdAmtTot; // 총금액
  private String svcAmt; // 봉사료
  private String taxAmt; // 부가세
  private String auNo; // 승인번호
  private String oriTrdUniKey; // 원거래승인번호
  private String cardTypeNm; // (NEW) 카드종류명
  private String issCd; // 발급사코드
  private String buyCd; // 매입사코드
  private String businessNo; // 사업자번호
  private String mchNo; // 가맹점번호
  private String ddcYn; // 매입구분
  private String checkYn; // 카드구분자
  private String icFeeRate; // (NEW) 현금 IC 가맹점수수료율
  private String icFee; // (NEW) 현금 IC 가맹점수수료
  private String icIssuerBankCd; // (NEW) 현금 IC 발급은행코드
  private String icAcquireBankCd; // (NEW) 현금 IC 매입은행코드
  private String usageInfo; // 업체사용정보
  private String filler; // filler

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
