/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;

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

        this.defaultServicesManagerImpl.save(r);
        
        assertEquals(2, this.defaultServicesManagerImpl.getAllServices().size());
        assertTrue(this.defaultServicesManagerImpl.getAllServices().contains(r));
    }
    
	public void testServicesEvaluationOrder() {
		final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
		final DefaultServicesManagerImpl localServiceManagerImpl = new DefaultServicesManagerImpl(dao);

		RegisteredServiceImpl sv1 = new RegisteredServiceImpl();
		sv1.setId(101);
		sv1.setServiceId("http://**");

		RegisteredServiceImpl sv2 = new RegisteredServiceImpl();
		sv2.setId(102);
		sv2.setServiceId("https://**");

		RegisteredServiceImpl sv3 = new RegisteredServiceImpl();
		sv3.setId(103);
		sv3.setServiceId("imaps://**");

		RegisteredServiceImpl sv4 = new RegisteredServiceImpl();
		sv4.setId(104);
		sv4.setServiceId("imap://**");

		RegisteredServiceImpl sv5 = new RegisteredServiceImpl();
		sv5.setId(105);
		sv5.setServiceId("http://com/?test.jsp");

		RegisteredServiceImpl sv6 = new RegisteredServiceImpl();
		sv6.setId(106);
		sv6.setServiceId("http://*.jsp");

		RegisteredServiceImpl sv7 = new RegisteredServiceImpl();
		sv7.setId(107);
		sv7.setServiceId("com/**/test.jsp");

		RegisteredServiceImpl sv8 = new RegisteredServiceImpl();
		sv8.setId(108);
		sv8.setServiceId("com/**/test?ng.jsp");

		RegisteredServiceImpl sv9 = new RegisteredServiceImpl();
		sv9.setId(109);
		sv9.setServiceId("com/**/????.js*");

		RegisteredServiceImpl sv10 = new RegisteredServiceImpl();
		sv10.setId(110);
		sv10.setServiceId("http://www.service.edu");

		localServiceManagerImpl.save(sv1);
		localServiceManagerImpl.save(sv2);
		localServiceManagerImpl.save(sv3);
		localServiceManagerImpl.save(sv4);
		localServiceManagerImpl.save(sv5);
		localServiceManagerImpl.save(sv6);
		localServiceManagerImpl.save(sv7);
		localServiceManagerImpl.save(sv8);
		localServiceManagerImpl.save(sv9);
		localServiceManagerImpl.save(sv10);

		List<RegisteredService> services = localServiceManagerImpl.getSortedServies();

		assertTrue(services.get(0).equals(sv10));
		assertTrue(services.get(1).equals(sv5));
		assertTrue(services.get(2).equals(sv6));
		assertTrue(services.get(3).equals(sv8));
		assertTrue(services.get(4).equals(sv7));
		assertTrue(services.get(5).equals(sv3));
		assertTrue(services.get(6).equals(sv2));
		assertTrue(services.get(7).equals(sv4));
		assertTrue(services.get(8).equals(sv1));
		assertTrue(services.get(9).equals(sv9));
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
