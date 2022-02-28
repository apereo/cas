package org.apereo.cas.config;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;
import lombok.NoArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;

import java.util.Optional;

/**
 * This is {@link HazelcastSessionPrincipalNameExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@NoArgsConstructor
public class HazelcastSessionPrincipalNameExtractor implements ValueExtractor<MapSession, String> {
    @Override
    public void extract(final MapSession target, final String argument, final ValueCollector collector) {
        Optional.ofNullable(target.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME))
            .ifPresent(collector::addObject);
    }
}
