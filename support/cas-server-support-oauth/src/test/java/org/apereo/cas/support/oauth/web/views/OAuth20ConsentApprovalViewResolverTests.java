package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ConsentApprovalViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OAuth")
class OAuth20ConsentApprovalViewResolverTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier("consentApprovalViewResolver")
    private ConsentApprovalViewResolver consentApprovalViewResolver;

    @Test
    void verifyBypassedBySession() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        oauthDistributedSessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        val service = getRegisteredService(randomServiceUrl(), UUID.randomUUID().toString(), "secret");
        servicesManager.save(service);
        assertFalse(consentApprovalViewResolver.resolve(context, service).hasView());
    }

    @Test
    void verifyRequireApproval() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
       
        val service = getRegisteredService(randomServiceUrl(), UUID.randomUUID().toString(), "secret");
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setUnauthorizedRedirectUrl(URI.create("https://example.com")));
        servicesManager.save(service);
        val modelAndView = consentApprovalViewResolver.resolve(context, service);
        assertTrue(modelAndView.hasView());
        assertTrue(modelAndView.getModel().containsKey("service"));
        assertTrue(modelAndView.getModel().containsKey("callbackUrl"));
        assertTrue(modelAndView.getModel().containsKey("deniedApprovalUrl"));
        assertTrue(modelAndView.getModel().containsKey("scopes"));
    }
}
