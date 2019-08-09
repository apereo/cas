package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.v2.ProxyController;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.spring5.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    AopAutoConfiguration.class,
    ProxyControllerTests.ProxyTestConfiguration.class,
    CasProtocolViewsConfiguration.class,
    CasValidationConfiguration.class
})
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {

    @Autowired
    @Qualifier("proxyController")
    private ProxyController proxyController;

    @Test
    public void verifyNoParams() {
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, this.proxyController
            .handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse()).getModel()
            .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService");
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey("code"));
    }

    @Test
    public void verifyExistingPGT() {
        val ticket = new ProxyGrantingTicketImpl(
            WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        getTicketRegistry().addTicket(ticket);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            CasProtocolConstants.PARAMETER_TICKET));
    }

    @Test
    public void verifyNotAuthorizedPGT() {
        val ticket = new ProxyGrantingTicketImpl(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        getTicketRegistry().addTicket(ticket);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "service");

        val map = this.proxyController.handleRequestInternal(request, new MockHttpServletResponse()).getModel();
        assertFalse(map.containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }

    @TestConfiguration
    public static class ProxyTestConfiguration {
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
