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
package org.jasig.cas.services.web;

import java.util.Collection;

import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class ManageRegisteredServicesMultiActionControllerTests {
    private ManageRegisteredServicesMultiActionController controller;

    private ServicesManager servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.controller = new ManageRegisteredServicesMultiActionController(this.servicesManager, "foo");
    }

    @Test
    public void verifyDeleteService() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("serviceId");
        r.setEvaluationOrder(1);

        this.servicesManager.save(r);

        final ModelAndView modelAndView = this.controller.deleteRegisteredService(1200);

        assertNotNull(modelAndView);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertEquals("deleted", modelAndView.getModel().get("status"));
        assertEquals("name", modelAndView.getModelMap().get("serviceName"));
    }

    public void verifyDeleteServiceNoService() {

        final ModelAndView modelAndView = this.controller.deleteRegisteredService(1200);
        assertNotNull(modelAndView);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertEquals("deleted", modelAndView.getModel().get("status"));
        assertEquals("", modelAndView.getModelMap().get("serviceName"));
    }

    public void verifyManage() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final ModelAndView modelAndView = this.controller.manage();

        assertNotNull(modelAndView);
        assertEquals("manageServiceView", modelAndView.getViewName());

        final Collection<?> c = (Collection<?>) modelAndView.getModel().get("services");
        assertTrue(c.contains(r));
    }

    @Test
    public void updateEvaluationOrderOK() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final ModelAndView modelAndView = this.controller.updateRegisteredServiceEvaluationOrder(r.getId(), 100);

        assertNotNull(modelAndView);
        assertEquals("jsonView", modelAndView.getViewName());

        final RegisteredService result = this.servicesManager.findServiceBy(r.getId());
        assertEquals(result.getEvaluationOrder(), 100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateEvaluationOrderInvalidServiceId() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);
        this.controller.updateRegisteredServiceEvaluationOrder(5000, 1000);
    }

}
