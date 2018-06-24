package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Slf4j
public class IgniteTicketRegistryWithEncryptionTests extends AbstractIgniteTicketRegistryTests {
    public IgniteTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
