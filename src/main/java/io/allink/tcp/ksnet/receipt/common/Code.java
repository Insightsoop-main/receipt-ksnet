package io.allink.tcp.ksnet.receipt.common;

import java.util.HashMap;
import java.util.Map;

/** Package: io.allink.tcp.koces.receipt.common Created: Devonshin Date: 29/03/2025 */
public class Code {

  public static final Map<String, String> TRD_TYPE_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "02", "승인",
                  "04", "취소",
                  "bq", "승인",
                  "bs", "취소",
                  "IA", "승인",
                  "IC", "취소",
                  "ZA", "승인",
                  "ZC", "취소"));
        }
      };

  public static final Map<String, String> PAY_TYPE_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "0", "일반",
                  "ZRP", "제로페이",
                  "SSP", "삼성페이",
                  "KKM", "카카오페이",
                  "KKO", "카카오페이",
                  "TSM", "토스페이",
                  "TSO", "토스페이"));
        }
      };

  public static final Map<String, String> SVC_TYPE_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.ofEntries(
                  Map.entry("02", "신용/포인트"),
                  Map.entry("04", "신용/포인트"),
                  Map.entry("bq", "현금영수증"),
                  Map.entry("bs", "현금영수증"),
                  Map.entry("IA", "현금IC"),
                  Map.entry("IC", "현금IC"),
                  Map.entry("ZA", "제로페이"),
                  Map.entry("ZC", "제로페이"),
                  Map.entry("00", "정상"),
                  Map.entry("91", "미등록가맹점"),
                  Map.entry("92", "태그미등록"),
                  Map.entry("93", "중복요청"),
                  Map.entry("99", "비정상")));
        }
      };

  public static final Map<String, String> SWIPE_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "S", "Swipe",
                  "K", "Key-in"));
        }
      };

  public static final Map<String, String> FOREIGN_YN_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "CCY", "해외카드",
                  "CCN", "국내카드",
                  "CIN", "일반가맹점",
                  "CIP", "공공가맹점",
                  "CIS", "영세가맹점"));
        }
      };

  public static final Map<String, String> DDC_YN_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "CB0", "개인",
                  "CB1", "법인",
                  "CC1", "DDC",
                  "CC0", "해당없음"));
        }
      };

  public static final Map<String, String> CHECK_YN_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "CCY", "체크카드",
                  "CCN", "신용카드",
                  "CB1", "거래취소",
                  "CB2", "오류발급",
                  "CB3", "기타",
                  "CIY", "캐쉬백 O",
                  "CIN", "캐쉬백 X"));
        }
      };

  public static final Map<String, String> CANCEL_TYPE_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "0", "일반취소",
                  "1", "망취소",
                  "2", "자동취소",
                  "a", "거래고유번호취소"));
        }
      };

  /*public static final Map<String, String> BANK_MAP = new HashMap<>() {{
    putAll(Map.of(
        "002",	"산업은행",
        "003",	"기업은행",
        "004",	"국민은행",
        "007",	"수협중앙",
        "011",	"농협은행",
        "012",	"지역농축",
        "020",	"우리은행",
        "023",	"한국SC"
    ));
    putAll(Map.of(
        "027",	"한국씨티",
        "029",	"국민은행",
        "031",	"대구은행",
        "032",	"부산은행",
        "034",	"광주은행",
        "035",	"제주은행",
        "037",	"전북은행",
        "039",	"경남은행",
        "045",	"새마을",
        "048",	"신협중앙"
    ));
    putAll(Map.of(
        "064",	"산림조합",
        "081",	"하나은행",
        "088",	"신한은행"
    ));
  }};*/
  /*
    public static final Map<String, String> NUMBER_TYPE_MAP = new HashMap<>() {{
      putAll(Map.of(
          "CC", "카드번호",
          "CB", "카드번호 or 식별번호",
          "CI", "IC일련번호",
          "PA", "바코드 or QR"
      ));
    }};
  */

  public static final Map<String, String> CARD_COMPANY_MAP =
      new HashMap<>() {
        {
          putAll(
              Map.of(
                  "01", "비씨카드",
                  "02", "국민카드",
                  "03", "외환카드",
                  "04", "삼성카드",
                  "05", "신한카드",
                  "08", "현대카드",
                  "09", "롯데카드",
                  "11", "시티카드",
                  "12", "수협카드",
                  "13", "신세계카드"));
          putAll(
              Map.of(
                  "14", "우리카드",
                  "15", "농협카드",
                  "16", "제주카드",
                  "17", "광주카드",
                  "18", "전북카드",
                  "24", "하나카드",
                  "25", "해외카드",
                  "26", "시티카드"));
        }
      };
}
