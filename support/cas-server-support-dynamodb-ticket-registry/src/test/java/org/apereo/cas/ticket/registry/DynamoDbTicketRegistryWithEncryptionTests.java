package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;


/**
 * This is {@link DynamoDbTicketRegistryWithEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 8000)
public class DynamoDbTicketRegistryWithEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
