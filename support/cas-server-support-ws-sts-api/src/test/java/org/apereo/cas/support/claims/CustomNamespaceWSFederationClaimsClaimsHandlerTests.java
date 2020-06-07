package org.apereo.cas.support.claims;

import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.val;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.request.TokenRequirements;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CustomNamespaceWSFederationClaimsClaimsHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
@SuppressWarnings("CollectionIncompatibleType")
public class CustomNamespaceWSFederationClaimsClaimsHandlerTests {

    @Test
    public void verifySupportedClaims() throws Exception {
        val handler = new CustomNamespaceWSFederationClaimsClaimsHandler("CAS", "https://apereo.org/cas",
            List.of("https://apereo.org/cas/givenName", "https://apereo.org/cas/email"));
        assertFalse(handler.getSupportedClaimTypes().isEmpty());
        assertTrue(handler.getSupportedClaimTypes().contains(new URI("https://apereo.org/cas/givenName")));
        assertTrue(handler.getSupportedClaimTypes().contains("https://apereo.org/cas/givenName"));
    }

    @Test
    public void verifySAML2Type() {
        val handler = new CustomNamespaceWSFederationClaimsClaimsHandler("CAS", "https://apereo.org/cas",
            List.of("https://apereo.org/cas/givenName", "https://apereo.org/cas/email"));

        val claims = new ClaimCollection();
        val claim = new Claim();
        claim.setClaimType("https://apereo.org/cas/givenName");
        claims.add(claim);

        val parameters = new ClaimsParameters();
        val requirements = new TokenRequirements();
        requirements.setTokenType(WSFederationConstants.WSS_SAML2_TOKEN_TYPE);
        parameters.setTokenRequirements(requirements);
        parameters.setRealm("CAS");
        parameters.setPrincipal(mock(Principal.class));
        val values = handler.retrieveClaimValues(claims, parameters);
        assertFalse(values.isEmpty());
        val processed = (ProcessedClaim) values.get(0);
        assertEquals("givenName", processed.getClaimType());
        assertEquals(handler.getIssuer(), processed.getIssuer());
    }

    @Test
    public void verifySAML1Type() {
        val handler = new CustomNamespaceWSFederationClaimsClaimsHandler("CAS", "https://apereo.org/cas",
            List.of("https://apereo.org/cas/givenName", "https://apereo.org/cas/email"));

        val claims = new ClaimCollection();
        val claim = new Claim();
        claim.setClaimType("https://apereo.org/cas/givenName");
        claims.add(claim);

        val parameters = new ClaimsParameters();
        val requirements = new TokenRequirements();
        requirements.setTokenType(WSFederationConstants.WSS_SAML1_TOKEN_TYPE);
        parameters.setTokenRequirements(requirements);
        parameters.setRealm("CAS");
        parameters.setPrincipal(mock(Principal.class));
        val values = handler.retrieveClaimValues(claims, parameters);
        assertFalse(values.isEmpty());
        val processed = (ProcessedClaim) values.get(0);
        assertEquals("https://apereo.org/cas/givenName", processed.getClaimType());
        assertEquals(handler.getIssuer(), processed.getIssuer());
    }

}
