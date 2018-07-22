package org.apereo.cas.support.events.dao;

import java.util.ArrayList;
import java.util.Collection;

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
        return new ArrayList<>(0);
    }
}
