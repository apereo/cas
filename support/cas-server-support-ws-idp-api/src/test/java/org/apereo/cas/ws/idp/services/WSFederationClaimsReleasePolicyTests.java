package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link WSFederationClaimsReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WSFederationClaimsReleasePolicyTests {

    @Test
    public void verifyAttributeReleaseNone() {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        final WSFederationClaimsReleasePolicy policy = new WSFederationClaimsReleasePolicy(
                CollectionUtils.wrap("uid", "casuser", "cn", "CAS"));
        final Principal principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("uid", "casuser", "cn", "CAS", "givenName", "CAS User"));
        final Map<String, Object> results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyAttributeRelease() {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        final WSFederationClaimsReleasePolicy policy = new WSFederationClaimsReleasePolicy(
                CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn", WSFederationClaims.EMAIL_ADDRESS.name(), "email"));
        final Principal principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("cn", "casuser", "email", "cas@example.org"));
        final Map<String, Object> results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertSame(results.size(), 2);
        assertTrue(results.containsKey(WSFederationClaims.COMMON_NAME.getUri()));
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS.getUri()));
    }
}
