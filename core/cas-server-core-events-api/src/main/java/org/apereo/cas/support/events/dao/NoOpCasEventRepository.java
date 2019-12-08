package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link NoOpCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpCasEventRepository extends AbstractCasEventRepository {

    /**
     * Static instance.
     */
    public static final CasEventRepository INSTANCE = new NoOpCasEventRepository();

    @Override
    public void save(final CasEvent event) {
    }

    @Override
    public Collection<CasEvent> load() {
        return new ArrayList<>(0);
    }
}
