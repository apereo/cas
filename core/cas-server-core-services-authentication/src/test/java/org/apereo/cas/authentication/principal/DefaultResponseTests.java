package org.apereo.cas.authentication.principal;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultResponseTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class DefaultResponseTests {
    @Test
    void verifyHeader() {
        val input = DefaultResponse.getHeaderResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.HEADER, input.responseType());
    }

    @Test
    void verifyRedirect() {
        val input = DefaultResponse.getRedirectResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.REDIRECT, input.responseType());
    }

    @Test
    void verifyPost() {
        val input = DefaultResponse.getPostResponse("github.com", Map.of("key", "value"));
        assertEquals(Response.ResponseType.POST, input.responseType());
    }
}
