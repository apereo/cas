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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

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
        
        return GoogleAccountsService.createServiceFrom(request, privateKey, publicKey, servicesManager);
    }

    @Before
    public void setUp() throws Exception {
        this.googleAccountsService = getGoogleAccountsService();
        this.googleAccountsService.setPrincipal(TestUtils.getPrincipal());
    }

    @Test
    public void verifyResponse() {
        final Response resp = this.googleAccountsService.getResponse("ticketId");
        assertEquals(resp.getResponseType(), Response.ResponseType.POST);
        assertTrue(resp.getAttributes().containsKey(SamlProtocolConstants.PARAMETER_SAML_RESPONSE));
        assertTrue(resp.getAttributes().containsKey(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
    }

    private static String encodeMessage(final String xmlString) throws IOException {
        return CompressionUtils.deflate(xmlString);
    }
}
