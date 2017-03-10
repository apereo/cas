package org.apereo.cas.support.wsfederation;

import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.junit.Test;
import org.opensaml.saml.saml1.core.Assertion;
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
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationHelperTests extends AbstractWsFederationTests {

    private static final String GOOD_TOKEN = "goodToken";

    @Autowired
    private WsFederationConfiguration wsFedConfig;
    
    @Autowired
    private HashMap<String, String> testTokens;

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void verifyParseTokenString() throws Exception {
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Assertion result = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        assertNotNull("testParseTokenString() - Not null", result);
    }

    @Test
    public void verifyCreateCredentialFromToken() throws Exception {
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        
        final WsFederationCredential expResult = new WsFederationCredential();
        expResult.setIssuedOn(ZonedDateTime.parse("2014-02-26T22:51:16.504Z"));
        expResult.setNotBefore(ZonedDateTime.parse("2014-02-26T22:51:16.474Z"));
        expResult.setNotOnOrAfter(ZonedDateTime.parse("2014-02-26T23:51:16.474Z"));
        expResult.setIssuer("http://adfs.example.com/adfs/services/trust");
        expResult.setAudience("urn:federation:cas");
        expResult.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        
        final WsFederationCredential result = wsFederationHelper.createCredentialFromToken(assertion);
        
        assertNotNull("testCreateCredentialFromToken() - Not Null", result);
        assertEquals("testCreateCredentialFromToken() - IssuedOn", expResult.getIssuedOn(), result.getIssuedOn());
        assertEquals("testCreateCredentialFromToken() - NotBefore", expResult.getNotBefore(), result.getNotBefore());
        assertEquals("testCreateCredentialFromToken() - NotOnOrAfter", expResult.getNotOnOrAfter(), result.getNotOnOrAfter());
        assertEquals("testCreateCredentialFromToken() - Issuer", expResult.getIssuer(), result.getIssuer());
        assertEquals("testCreateCredentialFromToken() - Audience", expResult.getAudience(), result.getAudience());
        assertEquals("testCreateCredentialFromToken() - Id", expResult.getId(), result.getId());
    }

    @Test
    public void verifyGetSigningCredential() throws Exception {

        final Credential result = wsFedConfig.getSigningCertificates().iterator().next();
        assertNotNull("testGetSigningCredential() - Not Null", result);        
    }


    @Test
    public void verifyValidateSignatureGoodToken() throws Exception {
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertTrue("testValidateSignatureGoodToken() - True", result);
    }

    @Test
    public void verifyValidateSignatureModifiedAttribute() throws Exception {
        final String wresult = testTokens.get("badTokenModifiedAttribute");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertFalse("testValidateSignatureModifiedAttribute() - False", result);
    }

    @Test
    @DirtiesContext
    public void verifyValidateSignatureBadKey() throws Exception {
        final List<Credential> signingWallet = new ArrayList<>();
        final WsFederationConfiguration cfg = new WsFederationConfiguration();
        cfg.setSigningCertificateResources(ctx.getResource("classpath:bad-signing.crt"));

        signingWallet.addAll(cfg.getSigningCertificates());
        final String wresult = testTokens.get(GOOD_TOKEN);
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        wsFedConfig.getSigningCertificates().clear();
        wsFedConfig.getSigningCertificates().addAll(signingWallet);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertFalse("testValidateSignatureModifiedKey() - False", result);
    }

    @Test
    public void verifyValidateSignatureModifiedSignature() throws Exception {
        final String wresult = testTokens.get("badTokenModifiedSignature");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult, wsFedConfig);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertFalse("testValidateSignatureModifiedSignature() - False", result);
    }

    public void setWsFedConfig(final WsFederationConfiguration config) {
        this.wsFedConfig = config;
    }

    public void setTestTokens(final HashMap<String, String> testTokens) {
        this.testTokens = testTokens;
    }
}
