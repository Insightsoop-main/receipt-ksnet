package io.allink.tcp.ksnet.receipt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        try {
            return jsonNode != null ? objectMapper.writeValueAsString(jsonNode) : null;
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 오류", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String json) {
        try {
            return json != null ? objectMapper.readTree(json) : null;
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 오류", e);
        }
    }
}

