package org.apereo.cas.dynamodb;

import org.apereo.cas.ticket.registry.BaseDynamoDbTicketRegistryFacilitatorTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("DynamoDb")
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbHealthIndicatorTests extends BaseDynamoDbTicketRegistryFacilitatorTests {

    @Autowired
    @Qualifier("dynamoDbHealthIndicator")
    private HealthIndicator dynamoDbHealthIndicator;

    @Test
    void verifyHealthOperation() {
        val health = dynamoDbHealthIndicator.health();
        val section = health.getDetails();
        assertTrue(section.containsKey("proxyTicketsTable"));
        assertTrue(section.containsKey("proxyGrantingTicketsTable"));
        assertTrue(section.containsKey("serviceTicketsTable"));
        assertTrue(section.containsKey("ticketGrantingTicketsTable"));
        assertTrue(section.containsKey("transientSessionTicketsTable"));
    }
}
