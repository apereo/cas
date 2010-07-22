/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;

import junit.framework.TestCase;

/**
 * 
 * @author battags
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.0
 *
 */
public class DefaultServicesManagerImplTests extends TestCase {
    
    private DefaultServicesManagerImpl defaultServicesManagerImpl;

    protected void setUp() throws Exception {
        final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
        final List<RegisteredService> list = new ArrayList<RegisteredService>();
        
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1);
        
        list.add(r);
        
        dao.setRegisteredServices(list);
        this.defaultServicesManagerImpl = new DefaultServicesManagerImpl(dao);
    }
    
    public void testSaveAndGet() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("test");
        r.setServiceId("test");
        
        this.defaultServicesManagerImpl.save(r);
        assertNotNull(this.defaultServicesManagerImpl.findServiceBy(1000));
    }
    
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
    
    public void testDeleteNotExistentService() {
        assertNull(this.defaultServicesManagerImpl.delete(1500));
    }
    
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
    
    protected class SimpleService implements Service {
        
        /**
         * Comment for <code>serialVersionUID</code>
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

        public void setPrincipal(Principal principal) {
            // nothing to do
        }

        public boolean logOutOfService(String sessionIdentifier) {
            return false;
        }
        
        public boolean matches(Service service) {
            return true;
        }
    }
}
