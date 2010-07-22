/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.TestUtils;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.zip.DeflaterOutputStream;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class GoogleAccountsServiceTests extends TestCase {

    private GoogleAccountsService googleAccountsService;

    public static GoogleAccountsService getGoogleAccountsService() throws Exception {
        final PublicKeyFactoryBean pubKeyFactoryBean = new PublicKeyFactoryBean();
        pubKeyFactoryBean.setAlgorithm("DSA");
        final PrivateKeyFactoryBean privKeyFactoryBean = new PrivateKeyFactoryBean();
        privKeyFactoryBean.setAlgorithm("DSA");
        
        final ClassPathResource pubKeyResource = new ClassPathResource("DSAPublicKey01.key");
        final ClassPathResource privKeyResource = new ClassPathResource("DSAPrivateKey01.key");
        
        pubKeyFactoryBean.setLocation(pubKeyResource);
        privKeyFactoryBean.setLocation(privKeyResource);
        pubKeyFactoryBean.afterPropertiesSet();
        privKeyFactoryBean.afterPropertiesSet();
        
        final DSAPrivateKey privateKey = (DSAPrivateKey) privKeyFactoryBean.getObject();
        final DSAPublicKey publicKey = (DSAPublicKey) pubKeyFactoryBean.getObject();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final String SAMLRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        request.setParameter("SAMLRequest", encodeMessage(SAMLRequest));
        
        return GoogleAccountsService.createServiceFrom(request, privateKey, publicKey, "username");
    }
    
    protected void setUp() throws Exception {       
        this.googleAccountsService = getGoogleAccountsService();
        this.googleAccountsService.setPrincipal(TestUtils.getPrincipal());
    }


    // XXX: re-enable when we figure out JVM requirements
    public void testResponse() {
        return;
    //    final Response response = this.googleAccountsService.getResponse("ticketId");
    //  assertEquals(ResponseType.POST, response.getResponseType());
    //    assertTrue(response.getAttributes().containsKey("SAMLResponse"));
    }

    
    protected static String encodeMessage(final String xmlString) throws IOException {
        byte[] xmlBytes = xmlString.getBytes("UTF-8");
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
          byteOutputStream);
        deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
        deflaterOutputStream.close();

        // next, base64 encode it
        Base64 base64Encoder = new Base64();
        byte[] base64EncodedByteArray = base64Encoder.encode(byteOutputStream
          .toByteArray());
        return new String(base64EncodedByteArray);
    }
}
