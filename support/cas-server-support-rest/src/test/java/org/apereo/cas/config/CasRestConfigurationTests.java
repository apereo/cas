package org.apereo.cas.config;

import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasRestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasThrottlingAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestAutoConfiguration.class
})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasRestConfigurationTests {

    @Autowired
    @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
    private TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    @Test
    void verifyOperation() {
        assertNotNull(ticketGrantingTicketResourceEntityResponseFactory);
    }
}
