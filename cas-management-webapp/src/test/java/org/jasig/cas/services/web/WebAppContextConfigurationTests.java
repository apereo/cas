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

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletResponse;

/**
 * Integration tests for the management webapp.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:*Context.xml")
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class WebAppContextConfigurationTests {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mvc;
    
    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }
    
    @Test
    public void verifyUpdateServiceSuccessfully() throws Exception {
        final MockHttpServletResponse response = this.mvc.perform(post("/updateRegisteredServiceEvaluationOrder.html")
                .param("id", "0"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("\"status\" : " + HttpServletResponse.SC_OK));
    }
    
    @Test
    public void verifyUpdateNonExistingService() throws Exception {
        this.mvc.perform(post("/updateRegisteredServiceEvaluationOrder.html")
                .param("id", "-1"))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    public void verifyDeleteServiceSuccessfully() throws Exception {
        final ModelAndView mv = this.mvc.perform(get("/deleteRegisteredService.html").param("id", "0"))
                .andExpect(status().isFound())
                .andReturn().getModelAndView();
        assertNotNull(mv);
        assertNotNull(mv.getModel().get("serviceName"));    
        assertEquals(mv.getModel().get("serviceName"), "HTTP and IMAP");        
    }
    
    @Test
    public void verifyDeleteNonExistingService() throws Exception {
        this.mvc.perform(get("/deleteRegisteredService.html").param("id", "100"))
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    public void verifyDeleteServiceByInvalidId() throws Exception {
        this.mvc.perform(get("/deleteRegisteredService.html").param("id", "invalid"))
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    public void verifyDeleteServiceByLargeId() throws Exception {
        this.mvc.perform(get("/deleteRegisteredService.html").param("id", String.valueOf(Double.MAX_VALUE)))
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    public void loadServices() throws Exception {
        final ModelAndView mv = this.mvc.perform(get("/manage.html"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();
        assertNotNull(mv);
        
        assertNotNull(mv.getModel().get("defaultServiceUrl"));
        final List<?> svcs = (List<?>) mv.getModel().get("services");
        assertEquals(svcs.size(), 1);
    }
}
