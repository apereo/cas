package org.apereo.cas.ticket.registry;

import org.apereo.cas.StringBean;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link JmsTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Parameterized.class)
public class JmsTicketRegistryTests extends AbstractTicketRegistryTests {

    public JmsTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        final JmsTemplate jms = Mockito.mock(JmsTemplate.class);
        return new JmsTicketRegistry(jms, new StringBean());
    }
}
