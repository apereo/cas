package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.test.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.test.junit.EnabledIfPortOpen;

/**
 * This is {@link DynamoDbTicketRegistryWithEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnabledIfPortOpen(port = 8000)
@EnabledIfContinuousIntegration
public class DynamoDbTicketRegistryWithEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithEncryptionTests() {
        super(true);
    }
}

