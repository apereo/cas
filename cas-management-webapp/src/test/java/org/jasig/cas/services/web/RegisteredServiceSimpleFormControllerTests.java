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

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link RegisteredServiceSimpleFormController}.
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class RegisteredServiceSimpleFormControllerTests {

    private RegisteredServiceSimpleFormController controller;

    private ReloadableServicesManager manager;

    private StubPersonAttributeDao repository;

    @Before
    public void setUp() throws Exception {
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put("test", Arrays.asList(new Object[] {"test"}));

        this.repository = new StubPersonAttributeDao();
        this.repository.setBackingMap(attributes);

        this.manager = new DefaultServicesManagerImpl(
                new InMemoryServiceRegistryDaoImpl());

        this.controller = new RegisteredServiceSimpleFormController(
                this.manager, this.repository);
    }

    @Test
    public void verifyAddRegisteredServiceNoValues() throws Exception {
        final BindingResult result = mock(BindingResult.class);
        when(result.getModel()).thenReturn(new HashMap<String, Object>());
        when(result.hasErrors()).thenReturn(true);
        
        final ModelMap model = new ModelMap();
        //this.controller.onSubmit(mock(RegisteredService.class), result, model, new MockHttpServletRequest());
        
        assertTrue(result.hasErrors());
    }

    @Test
    public void verifyAddRegisteredServiceWithValues() throws Exception {
        final RegisteredServiceImpl svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("serviceId");
        svc.setName("name");
        svc.setEvaluationOrder(123);
        
        assertTrue(this.manager.getAllServices().isEmpty());
        final RegisteredServiceEditBean data = RegisteredServiceEditBean.fromRegisteredService(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data.getServiceData(), mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for(final RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof RegexRegisteredService);
        }
    }

    @Test
    public void verifyEditRegisteredServiceWithValues() throws Exception {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("test");
        r.setDescription("description");

        this.manager.save(r);

        final RegisteredServiceImpl svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("serviceId1");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean data = RegisteredServiceEditBean.fromRegisteredService(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data.getServiceData(), mock(BindingResult.class));

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
    }

   @Test
    public void verifyAddRegexRegisteredService() throws Exception {
        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

       final RegisteredServiceEditBean data = RegisteredServiceEditBean.fromRegisteredService(svc);
       this.controller.saveService(new MockHttpServletRequest(),
               new MockHttpServletResponse(),
               data.getServiceData(), mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for(final RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof RegexRegisteredService);
        }
    }

    @Test
    public void verifyAddMultipleRegisteredServiceTypes() throws Exception {
        AbstractRegisteredService svc = new RegexRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean data = RegisteredServiceEditBean.fromRegisteredService(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data.getServiceData(), mock(BindingResult.class));

        svc = new RegisteredServiceImpl();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(100);
        svc.setEvaluationOrder(100);

        final RegisteredServiceEditBean data2 = RegisteredServiceEditBean.fromRegisteredService(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data2.getServiceData(), mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyAddMockRegisteredService() throws Exception {
        final RegexRegisteredService svc = new RegexRegisteredService();
        svc.setDescription("description");
        svc.setServiceId("^serviceId");
        svc.setName("name");
        svc.setId(1000);
        svc.setEvaluationOrder(1000);

        final RegisteredServiceEditBean data = RegisteredServiceEditBean.fromRegisteredService(svc);
        this.controller.saveService(new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                data.getServiceData(), mock(BindingResult.class));

        final Collection<RegisteredService> services = this.manager.getAllServices();
        assertEquals(1, services.size());
        for (final  RegisteredService rs : this.manager.getAllServices()) {
            assertTrue(rs instanceof RegisteredService);
        }
    }

    
    @Test
    public void verifyEditMockRegisteredService() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("test");
        r.setDescription("description");

        this.manager.save(r);
        
        r.setServiceId("serviceId1");
        //this.controller.onSubmit(r, mock(BindingResult.class), new ModelMap(), new MockHttpServletRequest());

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);

        assertEquals("serviceId1", r2.getServiceId());
        assertTrue(r2 instanceof RegexRegisteredService);
    }
}
