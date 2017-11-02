package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
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
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = {CasValidationConfiguration.class,
        ProxyControllerTests.ProxyTestConfiguration.class,
        CasProtocolViewsConfiguration.class,
        AbstractCentralAuthenticationServiceTests.CasTestConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreLogoutConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        AopAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreValidationConfiguration.class})
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {

    @Autowired
    @Qualifier("proxyController")
    private ProxyController proxyController;

    @Test
    public void verifyNoParams() {
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, this.proxyController
                .handleRequestInternal(new MockHttpServletRequest(),
                        new MockHttpServletResponse()).getModel()
                .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService");
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey("code"));
    }

    @Test
    public void verifyExistingPGT() {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl(
                WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                CasProtocolConstants.PARAMETER_TICKET));
    }

    @Test
    public void verifyNotAuthorizedPGT() {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter("targetService", "service");

        final Map<String, Object> map = this.proxyController.handleRequestInternal(request, new MockHttpServletResponse()).getModel();
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
