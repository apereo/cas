package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public class IgniteTicketRegistryWithoutEncryptionTests extends AbstractIgniteTicketRegistryTests {

    public IgniteTicketRegistryWithoutEncryptionTests() {
        super(false);
    }
}
