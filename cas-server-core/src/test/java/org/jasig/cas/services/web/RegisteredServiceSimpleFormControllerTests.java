/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.services.Attribute;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.MapBackedAttributeRepository;
import org.jasig.cas.services.MockServiceRegistryDao;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.support.RegisteredServiceValidator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class RegisteredServiceSimpleFormControllerTests extends TestCase {

    private RegisteredServiceSimpleFormController controller;

    private ServicesManager manager;

    private MapBackedAttributeRepository repository;

    @Override
    protected void setUp() throws Exception {
        final Map<String, Attribute> attributes = new HashMap<String, Attribute>();
        attributes.put("test", new Attribute("test", "test"));

        this.repository = new MapBackedAttributeRepository();
        this.repository.setAttributesMap(attributes);

        this.manager = new DefaultServicesManagerImpl(
            new MockServiceRegistryDao());
        
        final RegisteredServiceValidator validator = new RegisteredServiceValidator();
        validator.setServicesManager(this.manager);

        this.controller = new RegisteredServiceSimpleFormController(
            this.manager, this.repository);
        this.controller.setCommandClass(RegisteredServiceImpl.class);
        this.controller.setCommandName("registeredService");
        this.controller.setValidator(validator);
    }

    public void testAddRegisteredServiceNoValues() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.setMethod("POST");

        final ModelAndView modelAndView = this.controller.handleRequest(
            request, response);

        final BindingResult result = (BindingResult) modelAndView
            .getModel()
            .get(
                "org.springframework.validation.BindingResult.registeredService");

        assertTrue(result.hasErrors());
    }

    public void testAddRegisteredServiceWithValues() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("description", "description");
        request.addParameter("serviceId", "serviceId");
        request.addParameter("name", "name");
        request.addParameter("theme", "theme");
        request.addParameter("allowedToProxy", "true");
        request.addParameter("enabled", "true");
        request.addParameter("ssoEnabled", "true");
        request.addParameter("anonymousAccess", "false");

        request.setMethod("POST");

        assertTrue(this.manager.getAllServices().isEmpty());

        this.controller.handleRequest(
            request, response);

        assertFalse(this.manager.getAllServices().isEmpty());
    }
    
    public void testEditRegisteredServiceWithValues() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setId(1000);
        r.setServiceId("test");
        r.setDescription("description");
        
        this.manager.save(r);

        request.addParameter("description", "description");
        request.addParameter("serviceId", "serviceId1");
        request.addParameter("name", "name");
        request.addParameter("theme", "theme");
        request.addParameter("allowedToProxy", "true");
        request.addParameter("enabled", "true");
        request.addParameter("ssoEnabled", "true");
        request.addParameter("anonymousAccess", "false");
        request.addParameter("id", "1000");

        request.setMethod("POST");

        this.controller.handleRequest(
            request, response);

        assertFalse(this.manager.getAllServices().isEmpty());
        final RegisteredService r2 = this.manager.findServiceBy(1000);
        
        assertEquals("serviceId1", r2.getServiceId());
    }
}
