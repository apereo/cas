package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCasClientRedirectActionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcCasClientRedirectActionBuilderTests extends AbstractOidcTests {
    private static final String CALLBACK_URL = "https://example.org/cas/oidc/callbackAuthorize?client_id=client&client_name=CasOAuthClient";

    @Test
    void verifyPromptNone() {
        val url = build(OidcConstants.PROMPT_NONE);
        assertTrue(url.endsWith('&' + CasProtocolConstants.PARAMETER_GATEWAY + "=true"));
        assertFalse(url.contains(CasProtocolConstants.PARAMETER_RENEW + '='));
    }

    @Test
    void verifyPromptLogin() {
        val url = build(OidcConstants.PROMPT_LOGIN);
        assertTrue(url.endsWith('&' + CasProtocolConstants.PARAMETER_RENEW + "=true"));
        assertFalse(url.contains(CasProtocolConstants.PARAMETER_GATEWAY + '='));
    }

    @Test
    void verifyNoPrompt() {
        val url = build(null);
        assertEquals(casProperties.getServer().getLoginUrl() + "?service=" + EncodingUtils.urlEncode(CALLBACK_URL), url);
    }

    private String build(final @Nullable String prompt) {
        val request = new MockHttpServletRequest();
        request.setRequestURI("/cas/oidc/authorize");
        if (prompt != null) {
            request.setQueryString(OAuth20Constants.PROMPT + '=' + prompt);
            request.addParameter(OAuth20Constants.PROMPT, prompt);
        }
        val url = oauthCasClientRedirectActionBuilder.build(request, CALLBACK_URL);
        assertTrue(url.startsWith(casProperties.getServer().getLoginUrl() + "?service=" + EncodingUtils.urlEncode(CALLBACK_URL)));
        return url;
    }
}
