package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ImportAutoConfiguration({
    CasPersonDirectoryAutoConfiguration.class,
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
})
@Tag("CAS")
class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {
    @Test
    void verifyNoParams() throws Throwable {
        val result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY))
            .andExpect(status().isOk())
            .andReturn();

        assertNotNull(result.getModelAndView());
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, result.getModelAndView().getModel().get("code"));
    }

    @Test
    void verifyNonExistentPGT() throws Throwable {
        val result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY)
                .param(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService")
                .param(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault"))
            .andExpect(status().isOk())
            .andReturn();

        assertNotNull(result.getModelAndView());
        assertTrue(result.getModelAndView().getModel().containsKey("code"));
    }

    @Test
    void verifyExistingPGT() throws Throwable {
        val ticket = new ProxyGrantingTicketImpl(
            WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        getTicketRegistry().addTicket(ticket);
        val result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY)
                .param(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId())
                .param(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault"))
            .andExpect(status().isOk())
            .andReturn();

        assertNotNull(result.getModelAndView());
        assertTrue(result.getModelAndView().getModel().containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }

    @Test
    void verifyNotAuthorizedPGT() throws Throwable {
        val ticket = new ProxyGrantingTicketImpl(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        getTicketRegistry().addTicket(ticket);
        val result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY)
                .param(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId())
                .param(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "service"))
            .andExpect(status().isOk())
            .andReturn();

        assertNotNull(result.getModelAndView());
        val map = result.getModelAndView().getModel();
        assertFalse(map.containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }

    @TestConfiguration(value = "ProxyTestConfiguration", proxyBeanMethods = false)
    private static class ProxyTestConfiguration {
        @Bean
        public SpringTemplateEngine springTemplateEngine() {
            return new SpringTemplateEngine();
        }

        @Bean
        public ThymeleafProperties thymeleafProperties() {
            return new ThymeleafProperties();
        }
    }
}
