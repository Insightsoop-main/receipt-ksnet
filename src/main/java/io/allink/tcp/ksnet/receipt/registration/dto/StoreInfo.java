package io.allink.tcp.ksnet.receipt.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class StoreInfo {

    @JsonProperty("businessNo")
    private String businessNo;

    @JsonProperty("name")
    private String name;

    @JsonProperty("addr1")
    private String addr1;

    @JsonProperty("addr2")
    private String addr2;

    @JsonProperty("tel")
    private String tel;

    @JsonProperty("ceoName")
    private String ceoName;
}
