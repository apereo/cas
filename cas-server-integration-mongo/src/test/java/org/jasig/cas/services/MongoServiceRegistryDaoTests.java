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

import org.jasig.cas.TestUtils;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link MongoServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/mongo-context.xml")
public class MongoServiceRegistryDaoTests {

    @Autowired
    private ServiceRegistryDao serviceRegistryDao;

    @Before
    public void setup() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        for (final RegisteredService service : services) {
            this.serviceRegistryDao.delete(service);
        }
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        }
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(list.get(i), results.get(i));
        }
        for (int i = 0; i < 5; i++) {
            this.serviceRegistryDao.delete(results.get(i));
        }
        assertTrue(this.serviceRegistryDao.load().isEmpty());
    }

    @Test
    public void verifyEdit()  {
          final RegisteredServiceImpl r = new RegisteredServiceImpl();
          r.setName("test");
          r.setServiceId("testId");
          r.setTheme("theme");
          r.setDescription("description");

          this.serviceRegistryDao.save(r);

          final List<RegisteredService> services = this.serviceRegistryDao.load();

          final RegisteredService r2 = services.get(0);

          r.setId(r2.getId());
          r.setTheme("mytheme");

          this.serviceRegistryDao.save(r);

          final RegisteredService r3 = this.serviceRegistryDao.findServiceById(r.getId());

          assertEquals(r, r3);
          assertEquals(r.getTheme(), r3.getTheme());
          this.serviceRegistryDao.delete(r);
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
        final RegisteredService r2 = this.serviceRegistryDao.save(r);
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
        final RegisteredService r2 = this.serviceRegistryDao.save(r);
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
        final RegisteredService r2 = this.serviceRegistryDao.save(r);
        assertEquals(r, r2);
    }


    private static RegisteredService buildService(final int i) {
        return TestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
    }


}
