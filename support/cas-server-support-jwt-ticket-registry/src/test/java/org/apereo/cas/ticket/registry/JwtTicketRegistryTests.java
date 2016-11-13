package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.config.JwtTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link JwtTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class, JwtTicketRegistryConfiguration.class})
@EnableScheduling
public class JwtTicketRegistryTests {
}
