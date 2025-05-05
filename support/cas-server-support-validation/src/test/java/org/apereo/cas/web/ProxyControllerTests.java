package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.v2.ProxyController;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    @Qualifier("proxyController")
    private ObjectProvider<ProxyController> proxyController;

    @Test
    void verifyNoParams() throws Throwable {
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, this.proxyController.getObject()
            .handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse()).getModel()
            .get("code"));
    }

    @Test
    void verifyNonExistentPGT() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService");
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault");

        assertTrue(this.proxyController.getObject().handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey("code"));
    }

    @Test
    void verifyExistingPGT() throws Throwable {
        val ticket = new ProxyGrantingTicketImpl(
            WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        getTicketRegistry().addTicket(ticket);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE, "testDefault");

        val response = new MockHttpServletResponse();
        assertTrue(this.proxyController.getObject().canHandle(request, response));
        assertTrue(this.proxyController.getObject().handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            CasProtocolConstants.PARAMETER_TICKET));
    }

    @Test
    void verifyNotAuthorizedPGT() throws Throwable {
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

    @TestConfiguration(value = "ProxyTestConfiguration", proxyBeanMethods = false)
    static class ProxyTestConfiguration {
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
