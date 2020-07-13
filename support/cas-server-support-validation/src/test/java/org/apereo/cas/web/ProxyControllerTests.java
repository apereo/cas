package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.v2.ProxyController;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.spring5.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
@Tag("Simple")
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {

    @Autowired
    @Qualifier("proxyController")
    private ObjectProvider<ProxyController> proxyController;

    @Test
    public void verifyNoParams() {
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, this.proxyController.getObject()
            .handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse()).getModel()
            .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService");
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault");

        assertTrue(this.proxyController.getObject().handleRequestInternal(request,
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

        assertTrue(this.proxyController.getObject().handleRequestInternal(request,
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

        val map = this.proxyController.getObject().handleRequestInternal(request, new MockHttpServletResponse()).getModel();
        assertFalse(map.containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }

    @TestConfiguration("ProxyTestConfiguration")
    @Lazy(false)
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
