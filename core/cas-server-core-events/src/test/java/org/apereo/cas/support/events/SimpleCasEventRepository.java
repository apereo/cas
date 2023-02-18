package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import java.util.*;
import java.util.stream.Stream;

public class SimpleCasEventRepository extends AbstractCasEventRepository {
    private final Map<String,CasEvent> events = new HashMap<>();

    public SimpleCasEventRepository(CasEventRepositoryFilter eventRepositoryFilter) {
        super(eventRepositoryFilter);
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        events.put(UUID.randomUUID().toString(),event);
        return event;
    }

    @Override
    public Stream<CasEvent> load() {
        return events.values().stream();
    }

    public void clearEvents(){
        events.clear();
    }

}
