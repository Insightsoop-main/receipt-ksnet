package io.allink.tcp.ksnet.receipt.util;

public class StringUtil {
    // 주어진 문자열을 지정된 길이로 맞추고, 모자란 부분을 '0'으로 채워 우측 정렬한다.
    public static String rightJustifyWithZero(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value.substring(value.length() - length); // 초과하면 오른쪽 일부만 반환
        }
        return String.format("%" + length + "s", value).replace(' ', '0');
    }

    // 주어진 문자열을 지정된 길이로 맞추고, 모자란 부분을 Space(' ')로 채워 좌측 정렬한다.
    public static String leftJustifyWithSpace(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value.substring(0, length); // 초과하면 왼쪽 일부만 반환
        }
        return String.format("%-" + length + "s", value);
    }

    // 기본적인 고정 길이 포맷 (기본은 Space Padding)
    public static String defaultVal(String value, String defaultVal) {
        if (value == null || value.isEmpty()) {
            return defaultVal;
        }
        return value;
    }

    // 기본적인 고정 길이 포맷 (기본은 Space Padding)
    public static String formatFixedLength(String value, int length) {
        return leftJustifyWithSpace(value, length);
    }

    // 카드 번호 마스킹 처리
    public static String maskCardNumber(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;  // 카드 번호가 null이거나 길이가 8자 미만이면 그대로 반환
        }

        // 첫 4자리와 마지막 4자리를 제외하고 나머지는 *로 처리
        String first4 = cardNo.substring(0, 4);
        String last4 = cardNo.substring(cardNo.length() - 4);
        String middle = cardNo.substring(4, cardNo.length() - 4);  // 중간 부분 추출

        // 중간 부분 마스킹 후 4자리마다 '-' 추가
        String maskedMiddle = middle.replaceAll(".", "*");
        maskedMiddle = maskedMiddle.replaceAll("(.{4})", "$1-");  // 4자리마다 "-" 추가

        // 마지막 "-"를 제거
        if (maskedMiddle.endsWith("-")) {
            maskedMiddle = maskedMiddle.substring(0, maskedMiddle.length() - 1);
        }

        // 첫 4자리 + 마스킹된 중간 부분 + 마지막 4자리 결합
        return first4 + "-" + maskedMiddle + "-" + last4;
    }

    // 사업자 번호
    public static String formatBusinessNo(String businessNo) {
        if (businessNo == null || businessNo.length() != 10 || !businessNo.matches("\\d{10}")) {
            throw new IllegalArgumentException("올바른 10자리 숫자 사업자번호를 입력하세요.");
        }
        return businessNo.replaceAll("(\\d{3})(\\d{2})(\\d{5})", "$1-$2-$3");
    }
}
