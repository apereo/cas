package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;


/**
 * This is {@link DynamoDbTicketRegistryWithEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 8000)
public class DynamoDbTicketRegistryWithEncryptionTests extends AbstractDynamoDbTicketRegistryTests {
    public DynamoDbTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
