package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TransientSessionTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.ticket.tst.number-of-uses=2",
    "cas.ticket.tst.time-to-kill-in-seconds=5"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class TransientSessionTicketExpirationPolicyBuilderTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyType() throws Throwable {
        val builder = new TransientSessionTicketExpirationPolicyBuilder(casProperties);
        val policy = builder.buildTicketExpirationPolicy();
        assertNotNull(policy);
        val ticket = new DefaultTransientSessionTicketFactory(builder).create(RegisteredServiceTestUtils.getService());
        assertFalse(ticket.isExpired());
        ticket.update();
        assertFalse(ticket.isExpired());
        ticket.update();
        assertTrue(ticket.isExpired());
    }
}
