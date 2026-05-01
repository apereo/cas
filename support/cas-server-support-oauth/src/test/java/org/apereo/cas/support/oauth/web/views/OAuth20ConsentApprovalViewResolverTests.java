package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private WebContext webContext;

    private OAuthRegisteredService registeredService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        webContext = new JEEContext(request, response);

        val profile = new BasicUserProfile();
        profile.setId("casuser");
        val profileManager = new ProfileManager(webContext, oauthDistributedSessionStore);
        profileManager.save(true, profile, false);

        registeredService = getRegisteredService(randomServiceUrl(), UUID.randomUUID().toString(), "secret");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setUnauthorizedRedirectUrl(URI.create("https://example.com")));
        servicesManager.save(registeredService);
    }

    @Test
    void verifyRequireApproval() throws Throwable {
        var modelAndView = consentApprovalViewResolver.resolve(webContext, registeredService);
        assertTrue(modelAndView.hasView());
        val model = modelAndView.getModel();
        assertTrue(model.containsKey("service"));
        assertTrue(model.containsKey("callbackUrl"));
        assertTrue(model.containsKey("deniedApprovalUrl"));
        assertTrue(model.containsKey("scopes"));
        assertTrue(model.containsKey("approvalKey"));
        assertTrue(model.containsKey("recordKey"));

        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, model.get("approvalKey").toString());
        modelAndView = consentApprovalViewResolver.resolve(webContext, registeredService);
        assertFalse(modelAndView.hasView());
    }
}
