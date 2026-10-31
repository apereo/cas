package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcCasAuthenticationInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
class OidcCasAuthenticationInterceptorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private OAuth20CasCallbackUrlResolver casCallbackUrlResolver;

    private TicketGrantingTicket ticketGrantingTicket;

    private CasCookieBuilder cookieBuilder;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Override
    @BeforeEach
    protected void initialize() throws Throwable {
        super.initialize();
        cookieBuilder = mock(CasCookieBuilder.class);
        ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(ticketGrantingTicket);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void verifyOperation() throws Throwable {
        when(cookieBuilder.retrieveCookieValue(request)).thenReturn(ticketGrantingTicket.getId());
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        val interceptor = newInterceptor();
        assertTrue(interceptor.preHandle(request, response, new Object()));

        request.removeAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE);
        request.setQueryString("prompt=login");
        request.addParameter(OAuth20Constants.PROMPT, OidcConstants.PROMPT_LOGIN);
        assertRedirectsToLogin(interceptor);
    }

    @Test
    void verifyPromptLoginInPostBody() throws Throwable {
        when(cookieBuilder.retrieveCookieValue(request)).thenReturn(ticketGrantingTicket.getId());
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        request.setMethod(HttpMethod.POST.name());
        request.addParameter(OAuth20Constants.PROMPT, OidcConstants.PROMPT_LOGIN);
        assertRedirectsToLogin(newInterceptor());
        assertTrue(response.getHeader(HttpHeaders.LOCATION).contains(CasProtocolConstants.PARAMETER_RENEW + "=true"));
    }

    @Test
    void verifyMaxAgeOperation() throws Throwable {
        request.addParameter(OidcConstants.MAX_AGE, "5");
        when(cookieBuilder.retrieveCookieValue(request)).thenReturn(ticketGrantingTicket.getId());
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE,
            ZonedDateTime.now(Clock.systemUTC()).minusSeconds(30));
        storeProfileIntoSession(request, profile);
        val interceptor = newInterceptor();
        assertRedirectsToLogin(interceptor);

        request.setParameter(OidcConstants.MAX_AGE, "60");
        response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(profile.getId(), ((OAuth20UserProfile) request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE)).getId());
    }

    @Test
    void verifyLoadNoProfileWhenNoTgtAvailable() throws Throwable {
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        assertRedirectsToLogin(newInterceptor());
    }

    private OidcCasAuthenticationInterceptor newInterceptor() {
        return new OidcCasAuthenticationInterceptor(oauthDistributedSessionStore, cookieBuilder, ticketRegistry,
            servicesManager, oauthRequestParameterResolver, casCallbackUrlResolver, oauthCasClientRedirectActionBuilder);
    }

    private void assertRedirectsToLogin(final OidcCasAuthenticationInterceptor interceptor) throws Throwable {
        response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        val location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        assertTrue(location.startsWith(casProperties.getServer().getLoginUrl() + '?' + CasProtocolConstants.PARAMETER_SERVICE + '='));
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }
}
