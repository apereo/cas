package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

/**
 * This is {@link DynamoDbTicketRegistryWithEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ConditionalIgnore(condition = RunningStandaloneCondition.class, port = 8000)
public class DynamoDbTicketRegistryWithEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
