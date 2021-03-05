package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    @Qualifier("samlIdPLogoutResponseObjectBuilder")
    private SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder;
    
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
        val ctx = SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build();
        assertTrue(samlSingleLogoutServiceMessageHandler.supports(ctx, service));
        assertEquals(0, samlSingleLogoutServiceMessageHandler.getOrder());
    }

    @Test
    @Order(2)
    public void verifySendByPost() {
        val service = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(3)
    public void verifyNoSaml() {
        val registeredService = getSamlRegisteredServiceForTestShib();
        servicesManager.save(registeredService);
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(4)
    public void verifySendByRedirect() {
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(5)
    public void verifySkipLogoutForOriginator() throws Exception {
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        val request = new MockHttpServletRequest();
        val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()),
            "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer(service.getId()),
            UUID.randomUUID().toString(),
            samlIdPLogoutResponseObjectBuilder.getNameID(NameID.EMAIL, "cas@example.org"));
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }
        val response = new MockHttpServletResponse();
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .httpServletRequest(Optional.of(request))
                .httpServletResponse(Optional.of(response))
                .build());
        assertFalse(result.isEmpty());
    }
}
