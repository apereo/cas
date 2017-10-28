package org.apereo.cas.support.wsfederation;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.junit.Test;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WsFederationHelper}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationHelperTests extends AbstractWsFederationTests {

    private static final String GOOD_TOKEN = "goodToken";

    @Autowired
    private Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    private HashMap<String, String> testTokens;

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void verifyParseTokenString() {
        final String wresult = testTokens.get(GOOD_TOKEN);

        final Pair<Assertion, WsFederationConfiguration> result = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);
        assertNotNull("testParseTokenString() - Not null", result);
    }

    @Test
    public void verifyCreateCredentialFromToken() {
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Pair<Assertion, WsFederationConfiguration> assertion = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);

        final WsFederationCredential expResult = new WsFederationCredential();
        expResult.setIssuedOn(ZonedDateTime.parse("2014-02-26T22:51:16.504Z"));
        expResult.setNotBefore(ZonedDateTime.parse("2014-02-26T22:51:16.474Z"));
        expResult.setNotOnOrAfter(ZonedDateTime.parse("2014-02-26T23:51:16.474Z"));
        expResult.setIssuer("http://adfs.example.com/adfs/services/trust");
        expResult.setAudience("urn:federation:cas");
        expResult.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");

        final WsFederationCredential result = wsFederationHelper.createCredentialFromToken(assertion.getKey());

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
        final Credential result = wsFederationConfigurations.iterator().next().getSigningWallet().iterator().next();
        assertNotNull("testGetSigningCredential() - Not Null", result);
    }


    @Test
    public void verifyValidateSignatureGoodToken() {
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Pair<Assertion, WsFederationConfiguration> assertion = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);

        final boolean result = wsFederationHelper.validateSignature(assertion);
        assertTrue("testValidateSignatureGoodToken() - True", result);
    }

    @Test
    public void verifyValidateSignatureModifiedAttribute() {
        final String wresult = testTokens.get("badTokenModifiedAttribute");
        final Pair<Assertion, WsFederationConfiguration> assertion = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);
        final boolean result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedAttribute() - False", result);
    }

    @Test
    @DirtiesContext
    public void verifyValidateSignatureBadKey() {
        final List<Credential> signingWallet = new ArrayList<>();
        final WsFederationConfiguration cfg = new WsFederationConfiguration();
        cfg.setSigningCertificateResources(ctx.getResource("classpath:bad-signing.crt"));

        signingWallet.addAll(cfg.getSigningWallet());
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Pair<Assertion, WsFederationConfiguration> assertion = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);

        assertion.getValue().getSigningWallet().clear();
        assertion.getValue().getSigningWallet().addAll(signingWallet);
        final boolean result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedKey() - False", result);
    }

    @Test
    public void verifyValidateSignatureModifiedSignature() {
        final String wresult = testTokens.get("badTokenModifiedSignature");
        final Pair<Assertion, WsFederationConfiguration> assertion = wsFederationHelper.buildAndVerifyAssertion(
                wsFederationHelper.getRequestSecurityTokenFromResult(wresult), wsFederationConfigurations);
        final boolean result = wsFederationHelper.validateSignature(assertion);
        assertFalse("testValidateSignatureModifiedSignature() - False", result);
    }

    public void setTestTokens(final HashMap<String, String> testTokens) {
        this.testTokens = testTokens;
    }
}
