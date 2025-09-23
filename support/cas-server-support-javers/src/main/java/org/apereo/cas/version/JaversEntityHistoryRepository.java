package org.apereo.cas.version;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link JaversEntityHistoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class JaversEntityHistoryRepository implements EntityHistoryRepository {
    private final Javers javers;

    @Override
    public Object save(final Object object) {
        LOGGER.trace("Committing entity [{}]", object);
        javers.commit("CAS", object);
        return object;
    }


    @Override
    public List<HistoricalEntity> getHistory(final Object object) {
        val query = QueryBuilder.byInstance(object).build();
        val shadows = javers.findShadows(query);
        return shadows
            .stream()
            .map(shadow -> {
                val commit = shadow.getCommitMetadata();
                val date = commit.getCommitDate();
                val entity = Objects.requireNonNull((Serializable) shadow.get());
                return new HistoricalEntity(entity, shadow.getCommitId().value(), date);
            })
            .toList();
    }

    @Override
    public String getChangelog(final Object object) {
        val query = QueryBuilder.byInstance(object).withChildValueObjects().build();
        val changes = javers.findChanges(query);
        return changes.prettyPrint();
    }
}
