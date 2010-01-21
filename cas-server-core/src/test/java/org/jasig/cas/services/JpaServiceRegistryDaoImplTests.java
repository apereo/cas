/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import static org.junit.Assert.*;

/**
 * 
 * @author battags
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
@ContextConfiguration(locations= {"classpath:jpaTestApplicationContext.xml"})
public class JpaServiceRegistryDaoImplTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired(required=true)
    private JpaServiceRegistryDaoImpl dao;

    @Test
    public void testSaveMethodWithNonExistentServiceAndNoAttributes() {
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
    public void testSaveMethodWithNonExistentServiceAndAttributes() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAllowedAttributes(Arrays.asList("Test"));
        
        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        
        assertEquals(r, r2);
        assertEquals(r2, r3);

        assertTrue(r.getAllowedAttributes().contains("Test"));
    }

    @Test
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

    @Test
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
        r.setAllowedAttributes(Arrays.asList("Test"));
        
        this.dao.save(r);
        
        final RegisteredService r3 = this.dao.findServiceById(r.getId());
        
        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());

        assertTrue(r3.getAllowedAttributes().contains("Test"));

        r.setAllowedAttributes(new ArrayList<String>());
        this.dao.save(r);
        final RegisteredService r4 = this.dao.findServiceById(r.getId());
        assertTrue(r4.getAllowedAttributes().isEmpty());
    }
}
