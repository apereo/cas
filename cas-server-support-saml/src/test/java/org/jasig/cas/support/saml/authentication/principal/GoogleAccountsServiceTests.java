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
package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.DefaultResponse;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class GoogleAccountsServiceTests extends AbstractOpenSamlTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        final String samlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
              + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
              + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
              + "ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        request.setParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, encodeMessage(samlRequest));
        request.setParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "RelayStateAddedHere");

        final RegisteredService regSvc = mock(RegisteredService.class);
        when(regSvc.getUsernameAttributeProvider()).thenReturn(new DefaultRegisteredServiceUsernameProvider());
        
        final ServicesManager servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(regSvc);
        
        return GoogleAccountsService.createServiceFrom(request, privateKey, publicKey);
    }

    @Before
    public void setUp() throws Exception {
        this.googleAccountsService = getGoogleAccountsService();
        this.googleAccountsService.setPrincipal(TestUtils.getPrincipal());
        this.googleAccountsService.setSkewAllowance(500);
    }

    @Test
    public void verifyResponse() {
        final Response resp = this.googleAccountsService.getResponse("ticketId");
        assertEquals(resp.getResponseType(), DefaultResponse.ResponseType.POST);
        assertTrue(resp.getAttributes().containsKey(SamlProtocolConstants.PARAMETER_SAML_RESPONSE));
        assertTrue(resp.getAttributes().containsKey(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
    }


    @Test
    public void verifyAuthnRequestEncoded() {

        final byte[] decodedBytes = CompressionUtils.decodeBase64ToByteArray("fVJNT+MwEL0j8R8s35M0BbTIaoK6IEQldolo2MPej"
                + "DOtpzh28Njt8u9xUxBwWK7PM+/LM7v41xu2BU/obMXLfMIZWOU6tOuKP7TX2Tm/qI+PZiR7M4h5DNrew3MECixtWhLjQ8Wjt8JJQ"
                + "hJW9kAiKLGc/7oV03wiBu+CU85wtriquDHuSTulTfe4edLQOTQGN0avtV6h3AwaEaUcJGd/3m1N97YWRBEWloK0IUGT8iwrJ1l50p"
                + "ZnYvpDnJ785ax5U/qJ9pDgO1uPhyESN23bZM3dsh0JttiB/52mK752bm0gV67fyzeSCLcJXklDwNmcCHxIBi+dpdiDX4LfooKH+9uK"
                + "6xAGEkWx2+3yD5pCFtGYHLqYAxVSEa/HZsUYzn+q9Hvr8l2a1x/ks+ITVf32Y/sgi6vGGVQvbJ663116kCGlCD6mENfO9zL8X63My"
                + "xHBLluNoyJaGkDhCqHjrKgPql9PIx3MKw==");
        final String inflated = CompressionUtils.inflate(decodedBytes);
        assertNotNull(inflated);

    }
    private static String encodeMessage(final String xmlString) throws IOException {
        return CompressionUtils.deflate(xmlString);
    }
}
