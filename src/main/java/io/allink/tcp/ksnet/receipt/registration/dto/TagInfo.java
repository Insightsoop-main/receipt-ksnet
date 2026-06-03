package io.allink.tcp.ksnet.receipt.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TagInfo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("tagName")
    private String tagName;

    @JsonProperty("terminalType")
    private String terminalType; // KSNET-CAT or KSNET-POS
}
