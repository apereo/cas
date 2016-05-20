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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link org.jasig.cas.services.InMemoryServiceRegistryDaoImpl}.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class InMemoryServiceRegistryDaoImplTests {

    @Test
    public void verifySave()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
    }

    @Test
    public void verifyLoadEmpty() {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        assertEquals(reg.load().size(), 0);
    }

    @Test
     public void verifySaveAndLoad()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.load().size(), 1);
    }

    @Test
    public void verifySaveAndFind()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.findServiceById(svc.getId()), svc);
    }

    @Test
    public void verifySaveAndDelete()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertTrue(reg.delete(svc));
        assertEquals(reg.load().size(), 0);
    }

    @Test
    public void verifyEdit()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        reg.save(r);

        final List<RegisteredService> services = reg.load();

        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        r.setTheme("mytheme");

        reg.save(r);

        final RegisteredService r3 = reg.findServiceById(r.getId());

        assertEquals(r, r3);
        assertEquals(r.getTheme(), r3.getTheme());
        reg.delete(r);
    }
}
