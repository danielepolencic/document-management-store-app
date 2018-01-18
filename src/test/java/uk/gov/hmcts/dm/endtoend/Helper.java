package uk.gov.hmcts.dm.endtoend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Collections;

public class Helper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Helper(){}

    public static String getThumbnailUrlFromResponse(MockHttpServletResponse response) throws IOException {
        final String path = "/_links/thumbnail/href";
        return getPathFromResponse(response, path);

    }

    public static String getBinaryUrlFromResponse(MockHttpServletResponse response) throws IOException {
        final String path = "/_links/binary/href";
        return getPathFromResponse(response, path);

    }

    public static String getSelfUrlFromResponse(MockHttpServletResponse response) throws IOException {
        final String path = "/_links/self/href";
        return getPathFromResponse(response, path);
    }

    private static String getPathFromResponse(MockHttpServletResponse response, String path) throws IOException {
        final String content = response.getContentAsString();
        return getNodeAtPath(path, content)
                .asText()
                .replace("http://localhost", "");
    }

    private static JsonNode getNodeAtPath(String path, String content) throws IOException {
        return MAPPER
                .readTree(content)
                .at("/_embedded/documents").get(0)
                .at(path);
    }

    public static HttpHeaders getHeaders() {
        return getHeaders("user");
    }

    public static HttpHeaders getHeaders(String user) {
        final HttpHeaders headers = new HttpHeaders();
        headers.putAll(ImmutableMap.of("Authorization", Collections.singletonList(user),
                "ServiceAuthorization", Collections.singletonList("sscs")));
        return headers;
    }
}
