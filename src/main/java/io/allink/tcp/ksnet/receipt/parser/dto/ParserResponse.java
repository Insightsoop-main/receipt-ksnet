package io.allink.tcp.ksnet.receipt.parser.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParserResponse {

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("resultData")
    private ResultData resultData;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @Getter
    @Builder
    public static class ResultData {
        @JsonProperty("generatedUuid")
        private String generatedUuid;

        @JsonProperty("htmlViewLink")
        private String htmlViewLink;

        @JsonProperty("dataReqLink")
        private String dataReqLink;
    }

    public static ParserResponse ok(String uuid) {
        return ParserResponse.builder()
                .resultCode("OK")
                .resultMessage("영수증 데이터 저장 완료")
                .resultData(ResultData.builder()
                        .generatedUuid(uuid)
                        .htmlViewLink("n/a")
                        .dataReqLink("n/a")
                        .build())
                .build();
    }

    public static ParserResponse duplicate(String uuid) {
        return ParserResponse.builder()
                .resultCode("OK")
                .resultMessage("중복 요청 - 기존 데이터 반환")
                .resultData(ResultData.builder()
                        .generatedUuid(uuid)
                        .htmlViewLink("n/a")
                        .dataReqLink("n/a")
                        .build())
                .build();
    }

    public static ParserResponse error(String errorCode, String errorMessage) {
        return ParserResponse.builder()
                .resultCode("NOT_OK")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
