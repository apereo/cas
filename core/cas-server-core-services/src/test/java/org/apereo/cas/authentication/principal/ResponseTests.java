package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.4.4
 */
public class ResponseTests {

    private static final String TICKET_PARAM = "ticket";
    private static final String TICKET_VALUE = "foobar";

    @Test
    public void verifyConstructionWithoutFragmentAndNoQueryString() {
        val url = "http://localhost:8080/foo";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "?ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithoutFragmentButHasQueryString() {
        val url = "http://localhost:8080/foo?test=boo";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "&ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndQueryString() {
        val url = "http://localhost:8080/foo?test=boo#hello";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?test=boo&ticket=foobar#hello", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndNoQueryString() {
        val url = "http://localhost:8080/foo#hello";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, TICKET_VALUE);
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?ticket=foobar#hello", response.getUrl());
    }

    @Test
    public void verifyUrlSanitization() {
        val url = "https://www.example.com\r\nLocation: javascript:\r\n\r\n<script>alert(document.cookie)</script>";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, "ST-12345");
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com Location: javascript: <script>alert(document.cookie)</script>?ticket=ST-12345",
            response.getUrl());
    }

    @Test
    public void verifyUrlWithUnicode() {
        val url = "https://www.example.com/πολιτικῶν";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, "ST-12345");
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com/πολιτικῶν?ticket=ST-12345", response.getUrl());
    }

    @Test
    public void verifyUrlWithUrn() {
        val url = "urn:applis-cri:java-sso";
        val attributes = new HashMap<String, String>();
        attributes.put(TICKET_PARAM, "ST-123456");
        val response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("urn:applis-cri:java-sso?ticket=ST-123456", response.getUrl());
    }
}
