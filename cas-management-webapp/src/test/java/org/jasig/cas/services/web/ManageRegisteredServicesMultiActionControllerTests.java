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
package org.jasig.cas.services.web;

import java.util.Collection;

import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ManageRegisteredServicesMultiActionControllerTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ManageRegisteredServicesMultiActionController controller;

    private ServicesManager servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.controller = new ManageRegisteredServicesMultiActionController(this.servicesManager, "foo");
    }

    @Test
    public void testDeleteService() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("serviceId");
        r.setEvaluationOrder(1);

        this.servicesManager.save(r);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "1200");

        final ModelAndView modelAndView = this.controller.deleteRegisteredService(request,
                new MockHttpServletResponse());

        assertNotNull(modelAndView);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertEquals("deleted", modelAndView.getModel().get("status"));
        assertEquals("name", modelAndView.getModelMap().get("serviceName"));
    }

    public void testDeleteServiceNoService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "1200");

        final ModelAndView modelAndView = this.controller.deleteRegisteredService(request,
                new MockHttpServletResponse());

        assertNotNull(modelAndView);
        assertNull(this.servicesManager.findServiceBy(1200));
        assertEquals("deleted", modelAndView.getModel().get("status"));
        assertEquals("", modelAndView.getModelMap().get("serviceName"));
    }

    public void testManage() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        final ModelAndView modelAndView = this.controller.manage(new MockHttpServletRequest(), new MockHttpServletResponse());

        assertNotNull(modelAndView);
        assertEquals("manageServiceView", modelAndView.getViewName());

        final Collection<?> c = (Collection<?>) modelAndView.getModel().get("services");
        assertTrue(c.contains(r));
    }

    @Test
    public void updateEvaluationOrderOK() {
        RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("id", String.valueOf(r.getId()));
        request.addParameter("evaluationOrder", "100");

        final ModelAndView modelAndView = this.controller.updateRegisteredServiceEvaluationOrder(request, new MockHttpServletResponse());

        assertNotNull(modelAndView);
        assertEquals("jsonView", modelAndView.getViewName());

        RegisteredService result = this.servicesManager.findServiceBy(r.getId());
        assertEquals(result.getEvaluationOrder(), 100);
    }

    @Test
    public void updateEvaluationOrderInvalidServiceId() {
        RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addParameter("id", "5000");
            request.addParameter("evaluationOrder", "1000");

            this.controller.updateRegisteredServiceEvaluationOrder(request, new MockHttpServletResponse());
        } catch (final IllegalArgumentException e) {
            //Exception expected; service id cannot be found
            logger.debug(e.getMessage(), e);
        }
    }

    @Test
    public void updateEvaluationOrderInvalidEvalOrder() {
        RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1200);
        r.setName("name");
        r.setServiceId("test");
        r.setEvaluationOrder(2);

        this.servicesManager.save(r);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("id", "1000");
        request.addParameter("evaluationOrder", "TEST");

        try {
            this.controller.updateRegisteredServiceEvaluationOrder(request, new MockHttpServletResponse());
        } catch (final IllegalArgumentException e) {
            //Exception expected; evaluation order is invalid
            logger.debug(e.getMessage(), e);
        }
    }

}
