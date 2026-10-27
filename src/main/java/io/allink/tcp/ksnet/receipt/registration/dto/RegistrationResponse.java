package io.allink.tcp.ksnet.receipt.registration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationResponse {

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("data")
    private ResponseData data;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    @Getter
    @Builder
    public static class ResponseData {
        @JsonProperty("storeId")
        private String storeId;

        @JsonProperty("terminalId")
        private String terminalId;
    }

    public static RegistrationResponse ok(String storeId, String terminalId) {
        return RegistrationResponse.builder()
                .resultCode("OK")
                .resultMessage("가맹점 저장 완료")
                .data(ResponseData.builder()
                        .storeId(storeId)
                        .terminalId(terminalId)
                        .build())
                .build();
    }

    public static RegistrationResponse deleted() {
        return RegistrationResponse.builder()
                .resultCode("OK")
                .resultMessage("가맹점 삭제 완료")
                .build();
    }

    public static RegistrationResponse error(String errorCode, String errorMessage) {
        return RegistrationResponse.builder()
                .resultCode("NOT_OK")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
