/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author battags
 * @since 3.0
 *
 */
public class DefaultServicesManagerImplTests  {

    private DefaultServicesManagerImpl defaultServicesManagerImpl;

    @Before
    public void setUp() throws Exception {
        final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
        final List<RegisteredService> list = new ArrayList<RegisteredService>();

        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);

        list.add(r);

        dao.setRegisteredServices(list);
        this.defaultServicesManagerImpl = new DefaultServicesManagerImpl(dao);
    }

    @Test
    public void testSaveAndGet() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("test");
        r.setServiceId("test");

        this.defaultServicesManagerImpl.save(r);
        assertNotNull(this.defaultServicesManagerImpl.findServiceBy(1000));
    }

    @Test
    public void testMultiServicesBySameName() {
        RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(666);
        r.setName("testServiceName");
        r.setServiceId("testServiceA");

        this.defaultServicesManagerImpl.save(r);

        r = new RegisteredServiceImpl();
        r.setId(999);
        r.setName("testServiceName");
        r.setServiceId("testServiceB");

        this.defaultServicesManagerImpl.save(r);

        /** Added 2 above, plus another that is added during @Setup **/
        assertEquals(3, this.defaultServicesManagerImpl.getAllServices().size());
    }

    @Test
    public void testSaveWithReturnedPersistedInstance() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000L);
        r.setName("test");
        r.setServiceId("test");

        final RegisteredService persistedRs = this.defaultServicesManagerImpl.save(r);
        assertNotNull(persistedRs);
        assertEquals(1000L, persistedRs.getId());
    }

    @Test
    public void testDeleteAndGet() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("test");
        r.setServiceId("test");

        this.defaultServicesManagerImpl.save(r);
        assertEquals(r, this.defaultServicesManagerImpl.findServiceBy(r.getId()));

        this.defaultServicesManagerImpl.delete(r.getId());
        assertNull(this.defaultServicesManagerImpl.findServiceBy(r.getId()));
    }

    @Test
    public void testDeleteNotExistentService() {
        assertNull(this.defaultServicesManagerImpl.delete(1500));
    }

    @Test
    public void testMatchesExistingService() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("test");
        r.setServiceId("test");

        final Service service = new SimpleService("test");
        final Service service2 = new SimpleService("fdfa");

        this.defaultServicesManagerImpl.save(r);

        assertTrue(this.defaultServicesManagerImpl.matchesExistingService(service));
        assertEquals(r, this.defaultServicesManagerImpl.findServiceBy(service));
        assertNull(this.defaultServicesManagerImpl.findServiceBy(service2));
    }

    @Test
    public void testAllService() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("test");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.defaultServicesManagerImpl.save(r);

        assertEquals(2, this.defaultServicesManagerImpl.getAllServices().size());
        assertTrue(this.defaultServicesManagerImpl.getAllServices().contains(r));
    }

    @Test
    public void testEmptyServicesRegistry() {
        final SimpleService s = new SimpleService("http://www.google.com");
        
        for (final RegisteredService svc : defaultServicesManagerImpl.getAllServices()) {
            defaultServicesManagerImpl.delete(svc.getId());
        }
        assertTrue(this.defaultServicesManagerImpl.getAllServices().size() == 0);
        assertNull(this.defaultServicesManagerImpl.findServiceBy(s));
        assertNull(this.defaultServicesManagerImpl.findServiceBy(1000));
    }
    
    @Test
    public void testEvaluationOrderOfServices() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(100);
        r.setName("test");
        r.setServiceId("test");
        r.setEvaluationOrder(200);

        final RegisteredServiceImpl r2 = new RegisteredServiceImpl();
        r2.setId(101);
        r2.setName("test");
        r2.setServiceId("test");
        r2.setEvaluationOrder(80);

        final RegisteredServiceImpl r3 = new RegisteredServiceImpl();
        r3.setId(102);
        r3.setName("Sample test service");
        r3.setServiceId("test");
        r3.setEvaluationOrder(80);

        this.defaultServicesManagerImpl.save(r);
        this.defaultServicesManagerImpl.save(r3);
        this.defaultServicesManagerImpl.save(r2);

        final List<RegisteredService> allServices = new ArrayList<RegisteredService>(
                this.defaultServicesManagerImpl.getAllServices());

        //We expect the 3 newly added services, plus the one added in setUp()
        assertEquals(4, allServices.size());

        assertEquals(allServices.get(0).getId(), r3.getId());
        assertEquals(allServices.get(1).getId(), r2.getId());
        assertEquals(allServices.get(2).getId(), r.getId());

    }

    protected class SimpleService implements Service {

        /**
         * Comment for <code>serialVersionUID</code>.
         */
        private static final long serialVersionUID = 6572142033945243669L;
        private String id;

        protected SimpleService(final String id) {
            this.id = id;
        }

        public Map<String, Object> getAttributes() {
            return null;
        }

        public String getId() {
            return this.id;
        }

        public void setPrincipal(final Principal principal) {
            // nothing to do
        }

        public boolean matches(final Service service) {
            return true;
        }
    }
}
