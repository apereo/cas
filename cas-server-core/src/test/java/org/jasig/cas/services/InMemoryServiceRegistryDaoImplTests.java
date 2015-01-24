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
}
