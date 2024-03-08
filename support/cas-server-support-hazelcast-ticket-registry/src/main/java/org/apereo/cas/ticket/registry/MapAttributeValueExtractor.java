package org.apereo.cas.ticket.registry;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;
import com.hazelcast.query.impl.getters.MultiResult;
import lombok.val;

/**
 * This is {@link MapAttributeValueExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class MapAttributeValueExtractor implements ValueExtractor<HazelcastTicketDocument, String> {

    @Override
    public void extract(final HazelcastTicketDocument ticketHolder,
                        final String attributeName,
                        final ValueCollector valueCollector) {
        val values = ticketHolder.getAttributes().get(attributeName);
        if (values != null) {
            valueCollector.addObject(new MultiResult<>(values));
        }
    }

}
