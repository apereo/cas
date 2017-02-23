package org.apereo.cas.support.events.dao;

import java.util.Collection;
import java.util.Collections;

/**
 * This is {@link NoOpCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpCasEventRepository extends AbstractCasEventRepository {

    @Override
    public void save(final CasEvent event) {
    }

    @Override
    public Collection<CasEvent> load() {
        return Collections.emptyList();
    }
}
