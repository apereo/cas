package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcProfileScopeToAttributesFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcProfileScopeToAttributesFilterTests extends AbstractOidcTests {

    @Test
    public void verifyOperationFilterWithoutOpenId() {
        final var service = getOidcRegisteredService();
        final var accessToken = mock(AccessToken.class);
        final var context = new J2EContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        final var original = CoreAuthenticationTestUtils.getPrincipal();
        final var principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertEquals(original, principal);
    }

    @Test
    public void verifyOperationFilterWithOpenId() {
        final var service = getOidcRegisteredService();
        final var accessToken = mock(AccessToken.class);
        when(accessToken.getScopes()).thenReturn(CollectionUtils.wrapList(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PHONE.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.ADDRESS.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()));

        service.getScopes().add(OidcConstants.StandardScopes.EMAIL.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.ADDRESS.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PHONE.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PROFILE.getScope());
        final var context = new J2EContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        final var original = CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap("email", "casuser@example.org", "address", "1234 Main Street",
                "phone", "123445677", "name", "CAS", "gender", "male"));
        final var principal = profileScopeToAttributesFilter.filter(CoreAuthenticationTestUtils.getService(),
            original, service, context, accessToken);
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("address"));
        assertTrue(principal.getAttributes().containsKey("gender"));
        assertTrue(principal.getAttributes().containsKey("email"));
        assertEquals(4, principal.getAttributes().size());
    }

    @Test
    public void verifyOperationRecon() {
        final var service = getOidcRegisteredService();
        service.getScopes().add(OidcConstants.StandardScopes.ADDRESS.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.CUSTOM.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.EMAIL.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.OFFLINE_ACCESS.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.OPENID.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PHONE.getScope());
        service.getScopes().add(OidcConstants.StandardScopes.PROFILE.getScope());
        profileScopeToAttributesFilter.reconcile(service);
        final var policy = service.getAttributeReleasePolicy();
        assertTrue(policy instanceof ChainingAttributeReleasePolicy);
        final var chain = (ChainingAttributeReleasePolicy) policy;
        assertEquals(5, chain.size());
    }
}
