package org.apereo.cas.support.oauth.web.response;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuthWeb")
class OAuth20DefaultCasClientRedirectActionBuilderTests extends AbstractOAuth20Tests {
    private static final String CALLBACK_URL = "https://example.org/cas/oauth2.0/callbackAuthorize?client_id=client&client_name=CasOAuthClient";

    @Test
    void verifyOperation() {
        val builder = new OAuth20DefaultCasClientRedirectActionBuilder("https://example.org/cas/login");
        val url = builder.build(new MockHttpServletRequest(), CALLBACK_URL);
        assertEquals("https://example.org/cas/login?service=" + EncodingUtils.urlEncode(CALLBACK_URL), url);
    }

    @Test
    void verifyLoginUrlWithQuery() {
        val builder = new OAuth20DefaultCasClientRedirectActionBuilder("https://example.org/cas/login?locale=de");
        val url = builder.build(new MockHttpServletRequest(), CALLBACK_URL);
        assertEquals("https://example.org/cas/login?locale=de&service=" + EncodingUtils.urlEncode(CALLBACK_URL), url);
    }

    @Test
    void verifyConfiguredLoginUrl() {
        val url = oauthCasClientRedirectActionBuilder.build(new MockHttpServletRequest(), CALLBACK_URL);
        assertEquals(casProperties.getServer().getLoginUrl() + "?service=" + EncodingUtils.urlEncode(CALLBACK_URL), url);
    }
}
