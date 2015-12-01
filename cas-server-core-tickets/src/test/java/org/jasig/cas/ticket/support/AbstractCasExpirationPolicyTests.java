package org.jasig.cas.ticket.support;

import static org.junit.Assert.*;

import org.apache.commons.lang3.SerializationUtils;
import org.jasig.cas.ticket.TicketState;
import org.junit.Test;

public class AbstractCasExpirationPolicyTests {
    @Test
    public void testSerialization() throws Exception {
        final SerializationCasExpirationPolicy policy = new SerializationCasExpirationPolicy();
        final SerializationCasExpirationPolicy clone = SerializationUtils.clone(policy);
        assertNotNull(clone.logger);
    }

    private static class SerializationCasExpirationPolicy extends AbstractCasExpirationPolicy {
        private static final long serialVersionUID = 7388739337739594137L;

        @Override
        public boolean isExpired(final TicketState ticketState) {
            return true;
        }
    }
}
