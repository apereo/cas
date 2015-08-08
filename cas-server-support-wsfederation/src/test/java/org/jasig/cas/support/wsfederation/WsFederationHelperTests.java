/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.wsfederation;

import org.jasig.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

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
    
    @Autowired
    private WsFederationConfiguration wsFedConfig;
    
    @Autowired
    private HashMap<String, String> testTokens;

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void testParseTokenString() throws Exception {
        final String wresult = testTokens.get("goodToken");
        final Assertion result = wsFederationHelper.parseTokenFromString(wresult);
        assertNotNull("testParseTokenString() - Not null", result);
    }

    @Test
    public void testCreateCredentialFromToken() throws Exception {
        final String wresult = testTokens.get("goodToken");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult);
        
        final WsFederationCredential expResult = new WsFederationCredential();
        expResult.setIssuedOn(new DateTime("2014-02-26T22:51:16.504Z").withZone(DateTimeZone.UTC));
        expResult.setNotBefore(new DateTime("2014-02-26T22:51:16.474Z").withZone(DateTimeZone.UTC));
        expResult.setNotOnOrAfter(new DateTime("2014-02-26T23:51:16.474Z").withZone(DateTimeZone.UTC));
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
    public void testGetSigningCredential() throws Exception {

        final Credential result = wsFedConfig.getSigningCertificates().iterator().next();
        assertNotNull("testGetSigningCredential() - Not Null", result);        
    }


    @Test
    public void testValidateSignatureGoodToken() throws Exception {
        final String wresult = testTokens.get("goodToken");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertTrue("testValidateSignatureGoodToken() - True", result);
    }

    @Test
    public void testValidateSignatureModifiedAttribute() throws Exception {
        final String wresult = testTokens.get("badTokenModifiedAttribute");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertFalse("testValidateSignatureModifiedAttribute() - False", result);
    }

    @Test
    @DirtiesContext
    public void testValidateSignatureBadKey() throws Exception {
        final List<Credential> signingWallet = new ArrayList<>();
        final WsFederationConfiguration cfg = new WsFederationConfiguration();
        cfg.setSigningCertificateFiles(ctx.getResource("classpath:bad-signing.crt"));

        signingWallet.addAll(cfg.getSigningCertificates());
        final String wresult = testTokens.get("goodToken");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult);
        wsFedConfig.getSigningCertificates().clear();
        wsFedConfig.getSigningCertificates().addAll(signingWallet);
        final boolean result = wsFederationHelper.validateSignature(assertion, wsFedConfig);
        assertFalse("testValidateSignatureModifiedKey() - False", result);
    }

    @Test
    public void testValidateSignatureModifiedSignature() throws Exception {
        final String wresult = testTokens.get("badTokenModifiedSignature");
        final Assertion assertion = wsFederationHelper.parseTokenFromString(wresult);
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
