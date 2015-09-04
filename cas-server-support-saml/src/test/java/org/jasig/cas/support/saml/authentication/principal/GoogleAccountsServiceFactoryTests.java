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

import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link GoogleAccountsServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactoryTests {
    private GoogleAccountsServiceFactory factory;

    @Before
    public void setUp() throws Exception {
        final PublicKeyFactoryBean pubKeyFactoryBean = new PublicKeyFactoryBean();
        final PrivateKeyFactoryBean privKeyFactoryBean = new PrivateKeyFactoryBean();

        pubKeyFactoryBean.setAlgorithm("DSA");
        privKeyFactoryBean.setAlgorithm("DSA");

        final ClassPathResource pubKeyResource = new ClassPathResource("DSAPublicKey01.key");
        final ClassPathResource privKeyResource = new ClassPathResource("DSAPrivateKey01.key");

        pubKeyFactoryBean.setLocation(pubKeyResource);
        privKeyFactoryBean.setLocation(privKeyResource);
        assertTrue(privKeyFactoryBean.getObjectType().equals(PrivateKey.class));
        assertTrue(pubKeyFactoryBean.getObjectType().equals(PublicKey.class));
        pubKeyFactoryBean.afterPropertiesSet();
        privKeyFactoryBean.afterPropertiesSet();

        final ServicesManager servicesManager = mock(ServicesManager.class);

        this.factory = new GoogleAccountsServiceFactory(pubKeyFactoryBean.getObject(),
                privKeyFactoryBean.getObject(), servicesManager);
    }
    @Test
    public void verifyNoService() {
        assertNull(factory.createService(new MockHttpServletRequest()));
    }


}
