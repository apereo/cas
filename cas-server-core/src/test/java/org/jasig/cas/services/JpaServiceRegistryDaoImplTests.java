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
