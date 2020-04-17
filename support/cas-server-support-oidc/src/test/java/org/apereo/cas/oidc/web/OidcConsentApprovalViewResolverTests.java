package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;                            
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcConsentApprovalViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcConsentApprovalViewResolverTests extends AbstractOidcTests {

    @Test
    public void verifyBypassedBySession() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val store = new JEESessionStore();
        val context = new JEEContext(request, response, store);
        store.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        val service = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://google.com");
        assertNotNull(consentApprovalViewResolver.resolve(context, service));
    }

    @Test
    public void verifyBypassedByPrompt() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");
        request.setQueryString(OidcConstants.PROMPT + '=' + OidcConstants.PROMPT_CONSENT);
        
        val response = new MockHttpServletResponse();
        val store = new JEESessionStore();
        val context = new JEEContext(request, response, store);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        assertNotNull(consentApprovalViewResolver.resolve(context, service));
    }

    @Test
    public void verifyBypassedWithoutPrompt() {
        val request = new MockHttpServletRequest();
        request.setRequestURI("https://cas.org/something");
        
        val response = new MockHttpServletResponse();
        val store = new JEESessionStore();
        val context = new JEEContext(request, response, store);

        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        assertNotNull(consentApprovalViewResolver.resolve(context, service));
    }
}
