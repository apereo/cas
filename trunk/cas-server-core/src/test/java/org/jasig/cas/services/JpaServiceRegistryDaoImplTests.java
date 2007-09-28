/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

import org.springframework.test.jpa.AbstractJpaTests;

/**
 * 
 * @author battags
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class JpaServiceRegistryDaoImplTests extends AbstractJpaTests {

    private JpaServiceRegistryDaoImpl dao;

    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }
    
    public void setDao(final JpaServiceRegistryDaoImpl dao) {
        this.dao = dao;
    }

    public void testSaveMethodWithNonExistantServiceAndNoAttributes() {
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
    
    public void testSaveMethodWithNonExistantServiceAndAttributes() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAllowedAttributes(new String[] {"Test"});
        
        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        
        assertEquals(r, r2);
        assertEquals(r2, r3);
        
        for (int i = 0; i < r3.getAllowedAttributes().length; i++) {
            if (r3.getAllowedAttributes()[i].equals("Test")) {
                return;
            }
        }
        fail("Attribute 'Test' expected in array of allowed attributes.");
    }
    
    public void testSaveMethodWithExistingServiceNoAttribute() {
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
        
        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());
    }
    
    public void testSaveMethodWithExistingServiceAddAttribute() {
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
        r.setAllowedAttributes(new String[] {"Test"});
        
        this.dao.save(r);
        
        final RegisteredService r3 = this.dao.findServiceById(r.getId());
        
        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());

        boolean found = false;
        for (int i = 0; i < r3.getAllowedAttributes().length; i++) {
            if (r3.getAllowedAttributes()[i].equals("Test")) {
                found = true;
            }
        }
        
        assertTrue(found);
        
        r.setAllowedAttributes(new String[0]);
        this.dao.save(r);
        final RegisteredService r4 = this.dao.findServiceById(r.getId());
        assertTrue(r4.getAllowedAttributes().length == 0);
    }
}
