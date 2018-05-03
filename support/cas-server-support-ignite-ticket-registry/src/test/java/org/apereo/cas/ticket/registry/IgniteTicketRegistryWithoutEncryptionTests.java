package org.apereo.cas.ticket.registry;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
public class IgniteTicketRegistryWithoutEncryptionTests extends AbstractIgniteTicketRegistryTests {

    public IgniteTicketRegistryWithoutEncryptionTests() {
        super(false);
    }
}
