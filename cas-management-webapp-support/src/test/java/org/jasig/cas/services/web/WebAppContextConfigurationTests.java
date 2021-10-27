package org.jasig.cas.services.web;

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

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the management webapp.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:spring-configuration/*.xml",
        "classpath:cas-management-servlet.xml",
        "classpath:managementConfigContext.xml"})
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
        this.mvc.perform(get("/manage.html"));
        final MockHttpServletResponse response = this.mvc.perform(post("/deleteRegisteredService.html")
                .param("id", "200"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("\"status\" : " + HttpServletResponse.SC_OK));
    }
    
    @Test
    public void verifyDeleteNonExistingService() throws Exception {
        this.mvc.perform(post("/deleteRegisteredService.html").param("id", "666"))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    public void verifyDeleteServiceByInvalidId() throws Exception {
        this.mvc.perform(post("/deleteRegisteredService.html").param("id", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void loadServicesManageView() throws Exception {
        final ModelAndView mv = this.mvc.perform(get("/manage.html"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView();
        assertNotNull(mv);
        
        assertNotNull(mv.getModel().get("defaultServiceUrl"));
        assertNotNull(mv.getModel().get("status"));
    }

    @Test
    public void loadServices() throws Exception {
        final MockHttpServletResponse response = this.mvc.perform(get("/getServices.html"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        final String resp = response.getContentAsString();
        assertTrue(resp.contains("services"));
        assertTrue(resp.contains("status"));
    }
}
