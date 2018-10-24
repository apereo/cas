package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.test.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.test.junit.EnabledIfPortOpen;

/**
 * This is {@link DynamoDbTicketRegistryWithoutEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 8000)
public class DynamoDbTicketRegistryWithoutEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithoutEncryptionTests() {
        super(false);
    }
}

