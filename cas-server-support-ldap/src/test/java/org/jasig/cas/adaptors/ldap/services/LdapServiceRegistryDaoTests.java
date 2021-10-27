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
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapServiceRegistryDao} class.
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/ldap-regservice-test.xml"})
public class LdapServiceRegistryDaoTests extends AbstractLdapTests {

    @Autowired
    private ServiceRegistryDao dao;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Before
    public void setUp() throws Exception {
        for (final RegisteredService service : this.dao.load()) {
            this.dao.delete(service);
        }
    }

    @Test
    public void verifyEmptyRegistry() {
        assertEquals(0, this.dao.load().size());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.dao.findServiceById(9999991));
    }

    @Test
    public void verifySavingServices() {
        this.dao.save(getRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.dao.save(getRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.dao.save(rs));

        final RegisteredService rs3 = this.dao.findServiceById(rs.getId());
        assertEquals(rs3.getName(), rs.getName());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getProxyPolicy(), rs.getProxyPolicy());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
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

    @Test
    public void verifySavingServiceChangesDn() {
        this.dao.save(getRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        final long originalId = rs.getId();
        assertNotNull(rs);
        rs.setId(666);
        assertNotNull(this.dao.save(rs));
        assertNotEquals(rs.getId(), originalId);
    }

    @Test
    public void verifyDeletingSingleService() throws Exception {
        final RegisteredService rs = getRegexRegisteredService();
        final RegisteredService rs2 = getRegisteredService();
        this.dao.save(rs2);
        this.dao.save(rs);
        List<RegisteredService> services = this.dao.load();
        this.dao.delete(rs2);

        services = this.dao.load();
        assertEquals(1, services.size());
        assertEquals(services.get(0).getId(), rs.getId());
        assertEquals(services.get(0).getName(), rs.getName());
    }

    @Test
    public void verifyDeletingServices() throws Exception {
        this.dao.save(getRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        for (final RegisteredService registeredService : services) {
            this.dao.delete(registeredService);
        }
        assertEquals(0, this.dao.load().size());
    }

    private RegisteredService getRegisteredService() {
        final AbstractRegisteredService rs = new RegisteredServiceImpl();
        rs.setName("Service Name1");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        rs.setDescription("Service description");
        rs.setServiceId("https://?.edu/**");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        rs.setRequiredHandlers(new HashSet<>(Arrays.asList("handler8", "handle92")));
        return rs;
    }

    private RegisteredService getRegexRegisteredService() {
        final AbstractRegisteredService rs  = new RegexRegisteredService();
        rs.setName("Service Name Regex");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        rs.setDescription("Service description");
        rs.setServiceId("^http?://.+");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setDescription("Here is another description");
        rs.setRequiredHandlers(new HashSet<>(Arrays.asList("handler1", "handler2")));
        return rs;
    }
}
