package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.http.callback.CallbackUrlResolver;
import org.pac4j.core.http.url.UrlResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcCasClientRedirectActionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcCasClientRedirectActionBuilderTests extends AbstractOidcTests {

    @Test
    public void verifyPromptNone() {
        verifyBuild("=none");
    }

    @Test
    public void verifyPromptLogin() {
        verifyBuild("=login");
    }

    private void verifyBuild(final String prompt) {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");
        request.setQueryString(OidcConstants.PROMPT + prompt);
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response, new JEESessionStore());

        val casClient = new CasClient(new CasConfiguration("https://caslogin.com"));
        val callback = mock(CallbackUrlResolver.class);
        when(callback.compute(any(), any(), anyString(), any())).thenReturn("https://caslogin.com");
        casClient.setCallbackUrlResolver(callback);
        casClient.setUrlResolver(mock(UrlResolver.class));
        val result = oauthCasClientRedirectActionBuilder.build(casClient, context);
        assertTrue(result.isPresent());
    }
}
