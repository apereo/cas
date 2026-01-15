package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.token.TokenConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTokenRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
class DefaultTokenRequestExtractorTests {

    @Test
    void verifyTokenFromParameter() {
        val request = new MockHttpServletRequest();
        request.addParameter(TokenConstants.PARAMETER_NAME_TOKEN, "test");
        val token = TokenRequestExtractor.defaultExtractor().extract(request);
        assertEquals("test", token);
    }

    @Test
    void verifyTokenFromHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, "test");
        val token = TokenRequestExtractor.defaultExtractor().extract(request);
        assertEquals("test", token);
    }

    @Test
    void verifyTokenNotFound() {
        val request = new MockHttpServletRequest();
        val token = TokenRequestExtractor.defaultExtractor().extract(request);
        assertNull(token);
    }
}
