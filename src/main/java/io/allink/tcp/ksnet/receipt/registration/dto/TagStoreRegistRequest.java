package io.allink.tcp.ksnet.receipt.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TagStoreRegistRequest {

    @JsonProperty("type")
    private String type; // U: Upsert, D: Delete

    @JsonProperty("tag")
    private TagInfo tag;

    @JsonProperty("store")
    private StoreInfo store;
}
