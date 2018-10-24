package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;

import lombok.val;
import org.junit.jupiter.api.Test;

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
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("uid", "casuser", "cn", "CAS", "givenName", "CAS User"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyAttributeRelease() {
        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new WSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.name(), "cn", WSFederationClaims.EMAIL_ADDRESS.name(), "email"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", "casuser", "email", "cas@example.org"));
        val results = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), service);
        assertSame(2, results.size());
        assertTrue(results.containsKey(WSFederationClaims.COMMON_NAME.getUri()));
        assertTrue(results.containsKey(WSFederationClaims.EMAIL_ADDRESS.getUri()));
    }
}
