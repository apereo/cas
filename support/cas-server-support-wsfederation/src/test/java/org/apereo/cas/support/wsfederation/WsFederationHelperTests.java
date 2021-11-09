package org.apereo.cas.support.wsfederation;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;

import lombok.Setter;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.xmlsec.signature.Signature;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link WsFederationHelper}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Setter
@Tag("WSFederation")
public class WsFederationHelperTests extends AbstractWsFederationTests {

    @Test
    public void verifyEncryptedToken() throws Exception {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val wresult = IOUtils.toString(new ClassPathResource("encryptedToken.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val result = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), service);
        assertNotNull(result.getKey());
    }

    @Test
    public void verifyParseTokenString() throws Exception {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val result = wsFederationHelper.buildAndVerifyAssertion(token, wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        assertNotNull(result, "testParseTokenString() - Not null");
    }

    @Test
    public void verifyCreateCredentialFromToken() throws Exception {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token, wsFederationConfigurations.toList(),
            RegisteredServiceTestUtils.getService());
        val expResult = new WsFederationCredential();
        expResult.setIssuedOn(ZonedDateTime.parse("2014-02-26T22:51:16.504Z"));
        expResult.setNotBefore(ZonedDateTime.parse("2014-02-26T22:51:16.474Z"));
        expResult.setNotOnOrAfter(ZonedDateTime.parse("2014-02-26T23:51:16.474Z"));
        expResult.setIssuer("http://adfs.example.com/adfs/services/trust");
        expResult.setAudience("urn:federation:cas");
        expResult.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");

        val result = wsFederationHelper.createCredentialFromToken(assertion.getKey());
        assertNotNull(result);
        assertEquals(expResult.getIssuedOn(), result.getIssuedOn());
        assertEquals(expResult.getNotBefore(), result.getNotBefore());
        assertEquals(expResult.getNotOnOrAfter(), result.getNotOnOrAfter());
        assertEquals(expResult.getIssuer(), result.getIssuer());
        assertEquals(expResult.getAudience(), result.getAudience());
        assertEquals(expResult.getId(), result.getId());
    }

    @Test
    public void verifyGetSigningCredential() {
        val result = wsFederationConfigurations.toList().get(0).getSigningWallet().iterator().next();
        assertNotNull(result);
    }

    @Test
    public void verifyValidateSignatureGoodToken() throws Exception {
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertTrue(result);
    }

    @Test
    public void verifyValidateSignatureBadInput() {
        assertFalse(wsFederationHelper.validateSignature(null));
        assertFalse(wsFederationHelper.validateSignature(Pair.of(null, null)));
        val config = wsFederationConfigurations.toList().get(0);
        val assertion = mock(Assertion.class);
        assertFalse(wsFederationHelper.validateSignature(Pair.of(assertion, config)));
        when(assertion.getSignature()).thenReturn(mock(Signature.class));
        assertFalse(wsFederationHelper.validateSignature(Pair.of(assertion, config)));
    }

    @Test
    public void verifyValidateSignatureModifiedAttribute() throws Exception {
        val wresult = IOUtils.toString(new ClassPathResource("badTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertFalse(result);
    }

    @Test
    public void verifyValidateSignatureBadKey() throws Exception {
        val cfg = new WsFederationConfiguration();
        cfg.setSigningCertificateResources(new ClassPathResource("bad-signing.crt"));
        val signingWallet = new ArrayList<>(cfg.getSigningWallet());
        val wresult = IOUtils.toString(new ClassPathResource("goodTokenResponse.txt").getInputStream(), StandardCharsets.UTF_8);
        val requestSecurityTokenFromResult = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(requestSecurityTokenFromResult,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val wallet = assertion.getValue().getSigningWallet();
        wallet.clear();
        wallet.addAll(signingWallet);
        val result = wsFederationHelper.validateSignature(assertion);
        assertFalse(result);
    }

    @Test
    public void verifyValidateSignatureModifiedSignature() throws Exception {
        val wresult = IOUtils.toString(new ClassPathResource("badTokenSignature.txt").getInputStream(), StandardCharsets.UTF_8);
        val token = wsFederationHelper.getRequestSecurityTokenFromResult(wresult);
        val assertion = wsFederationHelper.buildAndVerifyAssertion(token,
            wsFederationConfigurations.toList(), RegisteredServiceTestUtils.getService());
        val result = wsFederationHelper.validateSignature(assertion);
        assertFalse(result);
    }
}
