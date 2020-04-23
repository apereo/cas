package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcProfileScopeToAttributesFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
public class OidcProfileScopeToAttributesFilterTests extends AbstractOidcTests {

    @Test
    public void verifyOAuth() {
        val service = getOAuthRegisteredService("example", "https://example.org");
        val accessToken = mock(OAuth20AccessToken.class);
        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal();
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertEquals(original, principal);
    }

    @Test
    public void verifyOperationFilterWithoutOpenId() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal();
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertEquals(original, principal);
    }

    @Test
    public void verifyOperationFilterWithOpenId() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getScopes()).thenReturn(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PHONE.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.ADDRESS.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()));

        service.getScopes().add(OidcConstants.StandardScopes.EMAIL.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.ADDRESS.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PHONE.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PROFILE.getScope());
        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("address"));
        assertTrue(principal.getAttributes().containsKey("gender"));
        assertTrue(principal.getAttributes().containsKey("email"));
        assertEquals(4, principal.getAttributes().size());
    }

    @Test
    public void verifyOperationFilterWithServiceDefinedScopes() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getScopes()).thenReturn(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PHONE.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.ADDRESS.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()));

        service.getScopes().clear();
        service.getScopes().add(OidcConstants.StandardScopes.EMAIL.getScope());

        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);

        assertTrue(principal.getAttributes().containsKey("email"));
        assertEquals(1, principal.getAttributes().size());
    }

    @Test
    public void verifyOperationFilterWithServiceDefinedReleasePolicy() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getScopes()).thenReturn(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PHONE.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.ADDRESS.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()));

        service.getScopes().clear();
        service.setAttributeReleasePolicy(new OidcProfileScopeAttributeReleasePolicy());

        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("gender"));
        assertEquals(2, principal.getAttributes().size());
    }

    @Test
    public void verifyByUserInfoClaims() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getClaims()).thenReturn(Map.of("userinfo", Map.of(
            "name", "{\"essential\": true}",
            "gender", "{\"essential\": true}")));
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getScopes()).thenReturn(CollectionUtils.wrapSet(OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        service.getScopes().clear();
        service.setAttributeReleasePolicy(new OidcProfileScopeAttributeReleasePolicy());

        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("gender"));
        assertEquals(2, principal.getAttributes().size());
    }

    @Test
    public void verifyAccessTokenNoScopes() {
        val service = getOidcRegisteredService();
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.OPENID.getScope()));
        service.setAttributeReleasePolicy(new OidcProfileScopeAttributeReleasePolicy());

        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        val principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("gender"));
        assertTrue(principal.getAttributes().containsKey("address"));
        assertTrue(principal.getAttributes().containsKey("phone"));
        assertTrue(principal.getAttributes().containsKey("email"));

    }
}
