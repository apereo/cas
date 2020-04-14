package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSingleLogoutServiceMessageHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SamlIdPSingleLogoutServiceMessageHandlerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlSingleLogoutServiceMessageHandler")
    private SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = getSamlRegisteredServiceForTestShib();
        servicesManager.save(samlRegisteredService);

        val service = new SamlRegisteredService();
        service.setName("Mocky");
        service.setServiceId("https://mocky.io");
        service.setId(101);
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        servicesManager.save(service);
    }

    @Test
    @Order(1)
    public void verifySupports() {
        val service = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        assertTrue(samlSingleLogoutServiceMessageHandler.supports(service));
        assertEquals(0, samlSingleLogoutServiceMessageHandler.getOrder());
    }

    @Test
    @Order(2)
    public void verifySendByPost() {
        val service = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            new MockTicketGrantingTicket("casuser"));
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(3)
    public void verifyNoSaml() {
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("example.org"));
        val service = RegisteredServiceTestUtils.getService("example.org");
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            new MockTicketGrantingTicket("casuser"));
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(4)
    public void verifySendByRedirect() {
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            new MockTicketGrantingTicket("casuser"));
        assertFalse(result.isEmpty());
    }
}
