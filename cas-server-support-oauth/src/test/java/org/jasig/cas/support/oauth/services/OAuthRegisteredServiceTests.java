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

package org.jasig.cas.support.oauth.services;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.JsonServiceRegistryDao;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class OAuthRegisteredServiceTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private final ServiceRegistryDao dao;

    public OAuthRegisteredServiceTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE.getFile());
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkCloning() {
        final AbstractRegisteredService r = new OAuthRegisteredService();
        r.setName("checkCloning");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final OAuthRegisteredService r2 = (OAuthRegisteredService) r.clone();
        assertEquals(r, r2);
    }

    @Test
    public void checkSaveMethod() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setClientId("clientid");
        r.setServiceId("secret");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof OAuthRegisteredService);
        this.dao.load();
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertTrue(r3 instanceof OAuthRegisteredService);
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }
}
