package org.apereo.cas.config;

import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasRestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasThrottlingAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestAutoConfiguration.class
})
@Tag("CasConfiguration")
class CasRestConfigurationTests {

    @Autowired
    @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
    private TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(ticketGrantingTicketResourceEntityResponseFactory);
    }
}
