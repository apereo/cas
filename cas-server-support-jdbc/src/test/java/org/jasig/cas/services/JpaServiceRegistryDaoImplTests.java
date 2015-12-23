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
package org.jasig.cas.services;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link JpaServiceRegistryDaoImpl}
 * @author battags
 * @since 3.1.0
 *
 */
@ContextConfiguration(locations= {"classpath:jpaTestApplicationContext.xml"})
public class JpaServiceRegistryDaoImplTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired(required=true)
    private JpaServiceRegistryDaoImpl dao;

    @Test
    public void verifySaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }
    
    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        this.dao.save(r);

        final List<RegisteredService> services = this.dao.load();

        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        r.setTheme("mytheme");

        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());

        assertEquals(r, r3);
        assertEquals(r.getTheme(), r3.getTheme());
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServicesCallback() {
        final OAuthCallbackAuthorizeService r = new OAuthCallbackAuthorizeService();
        r.setName("test345");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthRegisteredServicesCallback() {
        final OAuthRegisteredCallbackAuthorizeService r = new OAuthRegisteredCallbackAuthorizeService();
        r.setName("testoauth");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

}
