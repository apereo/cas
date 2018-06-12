package org.apereo.cas.support.wsfederation;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.junit.Test;
import org.opensaml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WsFederationHelper}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@Setter
public class WsFederationHelperTests extends AbstractWsFederationTests {
    private static final String GOOD_TOKEN = "goodToken";

    @Autowired
    private HashMap<String, String> testTokens;

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void verifyParseTokenString() {
        final var wresult = testTokens.get(GOOD_TOKEN);
        final var result =
            wsFederationHelper.buildAndVerifyAssertion(wsFederationHelper.getRequestSecurityTokenFromResult(wresult),
                wsFederationConfigurations);
        assertNotNull("testParseTokenString() - Not null", result);
    }

    @Test
    public void verifyCreateCredentialFromToken() {
        final var wresult = testTokens.get(GOOD_TOKEN);
        final var assertion =
            wsFederationHelper.buildAndVerifyAssertion(wsFederationHelper.getRequestSecurityTokenFromResult(wresult),
                wsFederationConfigurations);
        final var expResult = new WsFederationCredential();
        expResult.setIssuedOn(ZonedDateTime.parse("2014-02-26T22:51:16.504Z"));
        expResult.setNotBefore(ZonedDateTime.parse("2014-02-26T22:51:16.474Z"));
        expResult.setNotOnOrAfter(ZonedDateTime.parse("2014-02-26T23:51:16.474Z"));
        expResult.setIssuer("http://adfs.example.com/adfs/services/trust");
        expResult.setAudience("urn:federation:cas");
        expResult.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        final var result = wsFederationHelper.createCredentialFromToken(assertion.getKey());
        assertNotNull("testCreateCredentialFromToken() - Not Null", result);
        assertEquals("testCreateCredentialFromToken() - IssuedOn", expResult.getIssuedOn(), result.getIssuedOn());
        assertEquals("testCreateCredentialFromToken() - NotBefore", expResult.getNotBefore(), result.getNotBefore());
        assertEquals("testCreateCredentialFromToken() - NotOnOrAfter", expResult.getNotOnOrAfter(), result.getNotOnOrAfter());
        assertEquals("testCreateCredentialFromToken() - Issuer", expResult.getIssuer(), result.getIssuer());
        assertEquals("testCreateCredentialFromToken() - Audience", expResult.getAudience(), result.getAudience());
        assertEquals("testCreateCredentialFromToken() - Id", expResult.getId(), result.getId());
    }

    @Test
    public void verifyGetSigningCredential() {
        final var result = wsFederationConfigurations.iterator().next().getSigningWallet().iterator().next();
        assertNotNull("testGetSigningCredential() - Not Null", result);
    }

    @Test
    public void verifyValidateSignatureGoodToken() {
        final var wresult = testTokens.get(GOOD_TOKEN);
        final var assertion =
            wsFederationHelper.buildAndVerifyAssertion(wsFederationHelper.getRequestSecurityTokenFromResult(wresult),
                wsFederationConfigurations);
        final var result = wsFederationHelper.validateSignature(assertion);
        assertTrue("testValidateSignatureGoodToken() - True", result);
    }

    @Test
    public void verifyValidateSignatureModifiedAttribute() {
        final var wresult = testTokens.get("badTokenModifiedAttribute");
        final var assertion =
            wsFederationHelper.buildAndVerifyAssertion(wsFederationHelper.getRequestSecurityTokenFromResult(wresult),
                wsFederationConfigurations);
        final var result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedAttribute() - False", result);
    }

    @Test
    @DirtiesContext
    public void verifyValidateSignatureBadKey() {
        final var cfg = new WsFederationConfiguration();
        cfg.setSigningCertificateResources(ctx.getResource("classpath:bad-signing.crt"));
        final List<Credential> signingWallet = new ArrayList<>(cfg.getSigningWallet());
        final var wResult = testTokens.get(GOOD_TOKEN);
        final var requestSecurityTokenFromResult = wsFederationHelper.getRequestSecurityTokenFromResult(wResult);
        final var assertion = wsFederationHelper.buildAndVerifyAssertion(requestSecurityTokenFromResult, wsFederationConfigurations);
        final var wallet = assertion.getValue().getSigningWallet();
        wallet.clear();
        wallet.addAll(signingWallet);
        final var result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedKey() - False", result);
    }

    @Test
    public void verifyValidateSignatureModifiedSignature() {
        final var wresult = testTokens.get("badTokenModifiedSignature");
        final var assertion =
            wsFederationHelper.buildAndVerifyAssertion(wsFederationHelper.getRequestSecurityTokenFromResult(wresult),
                wsFederationConfigurations);
        final var result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedSignature() - False", result);
    }
}
