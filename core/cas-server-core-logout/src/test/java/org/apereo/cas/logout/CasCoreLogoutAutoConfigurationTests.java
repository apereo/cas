package org.apereo.cas.logout;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreLogoutAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = CasCoreLogoutAutoConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.ticket.track-descendant-tickets=true")
@Tag("Logout")
class CasCoreLogoutAutoConfigurationTests {
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

    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
