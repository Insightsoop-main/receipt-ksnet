package io.allink.tcp.ksnet.receipt.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.allink.tcp.ksnet.receipt.model.Store;
import io.allink.tcp.ksnet.receipt.protocol.KsnetMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class NormalizedPayloadBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * KsnetMessage + Store 로부터 normalized_payload JSON 문자열 생성
     */
    public static String build(Store store, KsnetMessage p) {
        try {
            // ── Mert 필드 ──────────────────────────────────────────────
            String storeName    = store != null ? nullToEmpty(store.getStoreName()) : "";
            String storeAddress = buildAddress(
                    store != null ? store.getAddr1() : null,
                    store != null ? store.getAddr2() : null);
            String businessNo   = store != null && !nullToEmpty(store.getBusinessNo()).isEmpty()
                    ? store.getBusinessNo()
                    : nullToEmpty(p.getBusinessNo());

            // ── PayInfos[0] 필드 ───────────────────────────────────────
            String cardNumber      = nullToEmpty(p.getCardNo());
            String cardType        = nullToEmpty(p.getCardTypeNm());
            String issuer          = nullToEmpty(p.getIssCd());
            String acquirer        = nullToEmpty(p.getBuyCd());
            String approvalNumber  = nullToEmpty(p.getAuNo());
            String installment     = nullToEmpty(p.getInsMon()).isEmpty() ? "일시불" : p.getInsMon();
            String terminalNumber  = maskTerminalId(p.getTermId());
            String merchantNumber  = nullToEmpty(p.getMchNo());

            // ── 금액 ───────────────────────────────────────────────────
            int total          = toInt(p.getTrdAmtTot());
            int tax            = toInt(p.getTaxAmt());
            int serviceAmount  = toInt(p.getSvcAmt());
            int amount         = total - tax - serviceAmount;

            // ── 거래구분 ───────────────────────────────────────────────
            // 취소는 원거래의 승인번호를 그대로 실어온다. 이 값이 없으면 소비 측에서
            // "같은 승인번호 = 중복"으로 오판해 취소 영수증이 사라진다. (ServerHandler에서 한글화됨)
            String trdType = nullToEmpty(p.getTrdType());

            // ── 거래일시 ───────────────────────────────────────────────
            String transactionDate = toKstIso(p.getTransDate(), p.getTransTime());

            NormalizedPayload np = NormalizedPayload.builder()
                    .vanType("KSNET_CAT")
                    .storeName(storeName)
                    .storeAddress(storeAddress)
                    .businessNumber(businessNo)
                    .cardNumber(cardNumber)
                    .cardType(cardType)
                    .issuer(issuer)
                    .acquirer(acquirer)
                    .approvalNumber(approvalNumber)
                    .installment(installment)
                    .terminalNumber(terminalNumber)
                    .merchantNumber(merchantNumber)
                    .vanMerchantNumber(merchantNumber)
                    .total(total)
                    .tax(tax)
                    .serviceAmount(serviceAmount)
                    .amount(amount)
                    .trdType(trdType)
                    .transactionDate(transactionDate)
                    .build();

            return objectMapper.writeValueAsString(np);

        } catch (JsonProcessingException e) {
            log.error("normalized_payload 생성 오류: {}", e.getMessage());
            // 생성 실패해도 INSERT 를 막지 않음 — 빈 객체 반환
            return "{}";
        }
    }

    // ── 헬퍼 메서드 ────────────────────────────────────────────────────

    private static String nullToEmpty(String v) {
        return v == null ? "" : v.trim();
    }

    private static String buildAddress(String addr1, String addr2) {
        return Stream.of(addr1, addr2)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(" "));
    }

    /** termId 앞 2자리를 ** 로 마스킹. 예: "0D29987006" → "**29987006" */
    private static String maskTerminalId(String termId) {
        if (termId == null || termId.length() < 2) return nullToEmpty(termId);
        return "**" + termId.substring(2);
    }

    /** null / 빈 문자열 → 0, 숫자 문자열 → parseInt (선행 0 포함 처리) */
    private static int toInt(String v) {
        if (v == null || v.isBlank()) return 0;
        try {
            String cleaned = v.trim().replace(",", "");
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * transDate(YYYYMMDD 또는 YYMMDD) + transTime(HHmmss)
     * → "YYYY-MM-DDTHH:mm:ss+09:00"
     */
    private static String toKstIso(String date, String time) {
        try {
            if (date == null || date.isBlank()) return "";
            String d = date.trim();
            String t = (time == null || time.isBlank()) ? "000000" : time.trim();

            String yyyy, mm, dd;
            if (d.length() == 8) {              // YYYYMMDD
                yyyy = d.substring(0, 4);
                mm   = d.substring(4, 6);
                dd   = d.substring(6, 8);
            } else if (d.length() == 6) {        // YYMMDD
                int yy = Integer.parseInt(d.substring(0, 2));
                yyyy = String.valueOf(yy >= 50 ? 1900 + yy : 2000 + yy);
                mm   = d.substring(2, 4);
                dd   = d.substring(4, 6);
            } else {
                return "";
            }

            String hh  = t.length() >= 2 ? t.substring(0, 2) : "00";
            String min = t.length() >= 4 ? t.substring(2, 4) : "00";
            String sec = t.length() >= 6 ? t.substring(4, 6) : "00";

            return yyyy + "-" + mm + "-" + dd + "T" + hh + ":" + min + ":" + sec + "+09:00";

        } catch (Exception e) {
            log.warn("toKstIso 변환 실패 date={} time={}: {}", date, time, e.getMessage());
            return "";
        }
    }

    // ── 표준 페이로드 DTO ──────────────────────────────────────────────

    @Getter
    @Builder(builderClassName = "NormalizedPayloadDtoBuilder")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class NormalizedPayload {
        @JsonProperty("van_type")           private String vanType;
        @JsonProperty("store_name")         private String storeName;
        @JsonProperty("store_address")      private String storeAddress;
        @JsonProperty("business_number")    private String businessNumber;
        @JsonProperty("card_number")        private String cardNumber;
        @JsonProperty("card_type")          private String cardType;
        @JsonProperty("issuer")             private String issuer;
        @JsonProperty("acquirer")           private String acquirer;
        @JsonProperty("approval_number")    private String approvalNumber;
        @JsonProperty("installment")        private String installment;
        @JsonProperty("terminal_number")    private String terminalNumber;
        @JsonProperty("merchant_number")    private String merchantNumber;
        @JsonProperty("van_merchant_number")private String vanMerchantNumber;
        @JsonProperty("total")              private int total;
        @JsonProperty("tax")                private int tax;
        @JsonProperty("service_amount")     private int serviceAmount;
        @JsonProperty("amount")             private int amount;
        @JsonProperty("trd_type")           private String trdType;   // 승인 | 취소
        @JsonProperty("transaction_date")   private String transactionDate;
    }
}
