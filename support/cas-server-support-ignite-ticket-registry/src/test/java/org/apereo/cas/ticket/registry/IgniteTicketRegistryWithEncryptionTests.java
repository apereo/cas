package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Slf4j
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public class IgniteTicketRegistryWithEncryptionTests extends AbstractIgniteTicketRegistryTests {

    public IgniteTicketRegistryWithEncryptionTests() {
        super(true);
    }
}
