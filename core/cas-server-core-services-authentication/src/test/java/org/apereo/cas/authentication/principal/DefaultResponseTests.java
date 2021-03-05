package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultResponseTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class DefaultResponseTests {
    @Test
    public void verifyHeader() {
        val input = DefaultResponse.getHeaderResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.HEADER, input.getResponseType());
    }

    @Test
    public void verifyRedirect() {
        val input = DefaultResponse.getRedirectResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.REDIRECT, input.getResponseType());
    }

    @Test
    public void verifyPost() {
        val input = DefaultResponse.getPostResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.POST, input.getResponseType());
    }
}
