package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
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

    private OAuthRegisteredService registeredService;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        val profile = new DefaultOAuth20UserProfile("casuser");
        storeProfileIntoSession(request, profile);

        registeredService = getRegisteredService(randomServiceUrl(), UUID.randomUUID().toString(), "secret");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setUnauthorizedRedirectUrl(URI.create("https://example.com")));
        servicesManager.save(registeredService);
    }

    @Test
    void verifyRequireApproval() throws Throwable {
        var modelAndView = consentApprovalViewResolver.resolve(request, response, registeredService);
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
        modelAndView = consentApprovalViewResolver.resolve(request, response, registeredService);
        assertFalse(modelAndView.hasView());
    }

    @Test
    void verifyApprovalCallbackKeepsRequestParameters() throws Throwable {
        request.setMethod(HttpMethod.GET.name());
        request.setRequestURI("/cas/oauth2.0/authorize");
        request.setQueryString("scope=openid+profile&state=a+b%2Bc");
        val getUrl = consentApprovalViewResolver.resolve(request, response, registeredService).getModel().get("callbackUrl").toString();
        assertTrue(getUrl.contains("scope=openid%20profile&state=a%20b%2Bc"), getUrl);

        request.setQueryString("scope=openid&" + OAuth20Constants.BYPASS_APPROVAL_PROMPT + "=true&" + OAuth20Constants.SCOPES_APPROVAL_KEY + "=stale");
        val retryUrl = consentApprovalViewResolver.resolve(request, response, registeredService).getModel().get("callbackUrl").toString();
        assertEquals(1, StringUtils.countMatches(retryUrl, OAuth20Constants.SCOPES_APPROVAL_KEY + '='), retryUrl);
        assertEquals(1, StringUtils.countMatches(retryUrl, OAuth20Constants.BYPASS_APPROVAL_PROMPT + '='), retryUrl);
        assertFalse(retryUrl.contains("stale"), retryUrl);

        val postRequest = new MockHttpServletRequest(HttpMethod.POST.name(), "/cas/oauth2.0/authorize");
        storeProfileIntoSession(postRequest, new DefaultOAuth20UserProfile("casuser"));
        postRequest.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        postRequest.addParameter(OAuth20Constants.REDIRECT_URI, "https://example.org/cb?x=1");
        val postUrl = consentApprovalViewResolver.resolve(postRequest, response, registeredService).getModel().get("callbackUrl").toString();
        assertTrue(postUrl.contains(OAuth20Constants.CLIENT_ID + '=' + registeredService.getClientId()), postUrl);
        assertTrue(postUrl.contains(OAuth20Constants.REDIRECT_URI + "=https%3A%2F%2Fexample.org%2Fcb%3Fx%3D1"), postUrl);
        assertTrue(postUrl.contains(OAuth20Constants.BYPASS_APPROVAL_PROMPT + "=true"), postUrl);
    }

    @Test
    void verifyApprovalCoversResources() throws Throwable {
        val withoutResources = consentApprovalViewResolver.resolve(request, response, registeredService).getModel().get("approvalKey").toString();

        request.addParameter(OAuth20Constants.RESOURCE, "https://api.example.org/");
        val modelAndView = consentApprovalViewResolver.resolve(request, response, registeredService);
        assertTrue(modelAndView.hasView());
        assertEquals(Set.of("https://api.example.org/"), modelAndView.getModel().get("resources"));
        val approvalKey = modelAndView.getModel().get("approvalKey").toString();
        assertNotEquals(withoutResources, approvalKey);

        request.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        request.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, withoutResources);
        assertTrue(consentApprovalViewResolver.resolve(request, response, registeredService).hasView());

        request.setParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, approvalKey);
        assertFalse(consentApprovalViewResolver.resolve(request, response, registeredService).hasView());
    }
}
