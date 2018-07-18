package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DynamoDbTicketRegistryWithEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 8000)
public class DynamoDbTicketRegistryWithEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
