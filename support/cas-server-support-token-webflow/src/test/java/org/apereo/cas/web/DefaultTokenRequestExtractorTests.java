package org.apereo.cas.web;

import org.apereo.cas.token.TokenConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultTokenRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultTokenRequestExtractorTests {

    @Test
    public void verifyTokenFromParameter() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(TokenConstants.PARAMETER_NAME_TOKEN, "test");
        final DefaultTokenRequestExtractor e = new DefaultTokenRequestExtractor();
        final String token = e.extract(request);
        assertEquals(token, "test");
    }

    @Test
    public void verifyTokenFromHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, "test");
        final DefaultTokenRequestExtractor e = new DefaultTokenRequestExtractor();
        final String token = e.extract(request);
        assertEquals(token, "test");
    }

    @Test
    public void verifyTokenNotFound() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final DefaultTokenRequestExtractor e = new DefaultTokenRequestExtractor();
        final String token = e.extract(request);
        assertNull(token);
    }
}
