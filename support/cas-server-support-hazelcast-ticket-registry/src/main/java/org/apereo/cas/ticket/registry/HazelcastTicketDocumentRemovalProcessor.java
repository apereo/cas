package org.apereo.cas.ticket.registry;

import module java.base;
import com.hazelcast.map.EntryProcessor;
import lombok.val;

/**
 * Removes the ticket documents it runs on, on the members that own them, and reports each removal,
 * so a bulk removal by query takes one pass and its count is the number of documents actually removed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class HazelcastTicketDocumentRemovalProcessor implements EntryProcessor<String, HazelcastTicketDocument, Boolean> {
    @Serial
    private static final long serialVersionUID = -2468159734582163941L;

    @Override
    public Boolean process(final Map.Entry<String, HazelcastTicketDocument> entry) {
        val removed = entry.getValue() != null;
        entry.setValue(null);
        return removed;
    }
}
