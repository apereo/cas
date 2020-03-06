package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;

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

    public NoOpCasEventRepository() {
        this(CasEventRepositoryFilter.noOp());
    }

    public NoOpCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter) {
        super(eventRepositoryFilter);
    }

    @Override
    public void saveInternal(final CasEvent event) {
        /*
         * A no-op event repository
         * does not save events.
         */
    }

    @Override
    public Collection<CasEvent> load() {
        return new ArrayList<>(0);
    }
}
