package io.allink.tcp.ksnet.receipt.parser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ParserRequest {

    @JsonProperty("source")
    private String source;          // Base64 인코딩된 영수증 데이터

    @JsonProperty("partnerReqUuid")
    private String partnerReqUuid;  // 이용기관 고유 UUID

    @JsonProperty("tagId")
    private String tagId;           // 태그 아이디

    @JsonProperty("storeUid")
    private String storeUid;        // 가맹점 고유 아이디 (선택)

    @JsonProperty("posId")
    private String posId;           // POS 구분 ID (선택)

    @JsonProperty("enc")
    private String enc;             // 인코딩 EUC-KR|UTF-8 (선택, 기본 EUC-KR)
}
