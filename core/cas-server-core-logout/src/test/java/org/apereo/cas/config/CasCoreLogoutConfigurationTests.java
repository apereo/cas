package org.apereo.cas.config;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.web.CasWebSecurityConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreLogoutConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreLogoutConfiguration.class
}, properties = "cas.ticket.track-descendant-tickets=true")
@Tag("CasConfiguration")
class CasCoreLogoutConfigurationTests {
    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("casProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<Void> casProtocolEndpointConfigurer;

    @Test
    void verifyOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(registeredService);

        val tgt = new MockTicketGrantingTicket("casuser");
        val st = tgt.grantServiceTicket(service, serviceTicketSessionTrackingPolicy);
        tgt.getDescendantTickets().add(st.getId());
        val results = logoutManager.performLogout(
            SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(tgt)
                .httpServletResponse(Optional.of(new MockHttpServletResponse()))
                .httpServletRequest(Optional.of(new MockHttpServletRequest()))
                .build());
        assertFalse(results.isEmpty());
        assertFalse(casProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }
}
