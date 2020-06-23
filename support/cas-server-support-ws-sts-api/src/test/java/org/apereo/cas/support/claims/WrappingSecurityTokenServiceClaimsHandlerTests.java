package org.apereo.cas.support.claims;

import org.apereo.cas.ws.idp.WSFederationClaims;

import lombok.val;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WrappingSecurityTokenServiceClaimsHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class WrappingSecurityTokenServiceClaimsHandlerTests {

    @Test
    public void verifySupportedClaims() {
        val handler = new WrappingSecurityTokenServiceClaimsHandler("CAS", "https://apereo.org/cas");
        assertFalse(handler.getSupportedClaimTypes().isEmpty());
        assertTrue(handler.getSupportedClaimTypes().contains(WSFederationClaims.COMMON_NAME.getUri()));
        assertTrue(handler.getSupportedRealms().contains("CAS"));
    }

    @Test
    public void verifyClaimMatchesRealm() {
        val claims = new ClaimCollection();

        val claim = new Claim();
        claim.setClaimType(WSFederationClaims.COMMON_NAME.getUri());
        claims.add(claim);

        val parameters = new ClaimsParameters();
        parameters.setRealm("CAS-Other");
        
        val handler = new WrappingSecurityTokenServiceClaimsHandler("CAS", "https://apereo.org/cas");
        assertTrue(handler.retrieveClaimValues(claims, parameters).isEmpty());
    }

    @Test
    public void verifyClaimNoPrincipal() {
        val claims = new ClaimCollection();

        val claim = new Claim();
        claim.setClaimType(WSFederationClaims.COMMON_NAME.getUri());
        claims.add(claim);

        val parameters = new ClaimsParameters();
        parameters.setRealm("CAS");

        val handler = new WrappingSecurityTokenServiceClaimsHandler("CAS", "https://apereo.org/cas");
        assertTrue(handler.retrieveClaimValues(claims, parameters).isEmpty());
    }

    @Test
    public void verifyClaimNoClaims() {
        val claims = new ClaimCollection();

        val parameters = new ClaimsParameters();
        parameters.setRealm("CAS");
        parameters.setPrincipal(mock(Principal.class));
        
        val handler = new WrappingSecurityTokenServiceClaimsHandler("CAS", "https://apereo.org/cas");
        assertTrue(handler.retrieveClaimValues(claims, parameters).isEmpty());
    }

    @Test
    public void verifyClaims() {
        val claims = new ClaimCollection();
        val claim = new Claim();
        claim.setClaimType(WSFederationClaims.COMMON_NAME.getUri());
        claims.add(claim);

        val parameters = new ClaimsParameters();
        parameters.setRealm("CAS");
        parameters.setPrincipal(mock(Principal.class));

        val handler = new WrappingSecurityTokenServiceClaimsHandler("CAS", "https://apereo.org/cas");
        assertFalse(handler.retrieveClaimValues(claims, parameters).isEmpty());
    }
}
