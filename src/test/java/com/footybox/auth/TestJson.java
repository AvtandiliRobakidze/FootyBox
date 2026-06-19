package com.footybox.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.test.web.servlet.MvcResult;

public final class TestJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestJson() {
    }

    public static String token(MvcResult result) throws IOException {
        JsonNode node = MAPPER.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    public static long longField(MvcResult result, String field) throws IOException {
        JsonNode node = MAPPER.readTree(result.getResponse().getContentAsString());
        return node.get(field).asLong();
    }
}
