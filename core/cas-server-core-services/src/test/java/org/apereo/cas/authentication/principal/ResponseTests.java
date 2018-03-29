package org.apereo.cas.authentication.principal;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.4.4
 */
@Slf4j
public class ResponseTests {

    private static final String TICKET_PARAM = "ticket";
    private static final String TICKET_VALUE = "foobar";

    @Test
    public void verifyConstructionWithoutFragmentAndNoQueryString() {
        final var url = "http://localhost:8080/foo";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "?ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithoutFragmentButHasQueryString() {
        final var url = "http://localhost:8080/foo?test=boo";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "&ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndQueryString() {
        final var url = "http://localhost:8080/foo?test=boo#hello";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?test=boo&ticket=foobar#hello", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndNoQueryString() {
        final var url = "http://localhost:8080/foo#hello";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?ticket=foobar#hello", response.getUrl());
    }

    @Test
    public void verifyUrlSanitization() {
        final var url = "https://www.example.com\r\nLocation: javascript:\r\n\r\n<script>alert(document.cookie)</script>";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, "ST-12345");
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com Location: javascript: <script>alert(document.cookie)</script>?ticket=ST-12345",
                response.getUrl());
    }

    @Test
    public void verifyUrlWithUnicode() {
        final var url = "https://www.example.com/πολιτικῶν";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, "ST-12345");
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com/πολιτικῶν?ticket=ST-12345", response.getUrl());
    }

    @Test
    public void verifyUrlWithUrn() {
        final var url = "urn:applis-cri:java-sso";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(TICKET_PARAM, "ST-123456");
        final var response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("urn:applis-cri:java-sso?ticket=ST-123456", response.getUrl());
    }
}
