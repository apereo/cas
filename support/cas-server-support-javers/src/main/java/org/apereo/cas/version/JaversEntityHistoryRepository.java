package org.apereo.cas.version;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.SnapshotType;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This is {@link JaversEntityHistoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class JaversEntityHistoryRepository implements EntityHistoryRepository {
    /**
     * The author of changes made while nobody is signed in, such as services loaded on a schedule or at startup.
     */
    public static final String SYSTEM_AUTHOR = "CAS";

    private static final int SNAPSHOTS_PER_CHANGE = 25;

    private static final int MAX_SNAPSHOTS = 500;

    private final Javers javers;

    /**
     * The author of a change: the name of the user signed in to the request that makes it, such as an administrator
     * saving an application from Palantir or calling the services endpoint, or {@value #SYSTEM_AUTHOR} when the
     * change is not made on behalf of a signed-in user.
     *
     * @return the author
     */
    protected String currentAuthor() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken
            || StringUtils.isBlank(authentication.getName())) {
            return SYSTEM_AUTHOR;
        }
        return authentication.getName();
    }

    @Override
    public Object save(final Object object) {
        val author = currentAuthor();
        LOGGER.trace("Committing entity [{}] by [{}]", object, author);
        javers.commit(author, object);
        return object;
    }


    @Override
    public void delete(final Object object) {
        val author = currentAuthor();
        LOGGER.trace("Committing deletion of entity [{}] by [{}]", object, author);
        javers.commitShallowDelete(author, object);
    }

    /**
     * The latest changes to any entity. Snapshots of the values inside an entity, such as its policies, are recorded
     * next to the entity's own, so more snapshots than asked for are read and only the entities' own are kept. The name
     * of a deleted entity comes from its last snapshot before the deletion, since the deletion records no state.
     *
     * @param limit how many changes to return at most
     * @return the changes, newest first
     */
    @Override
    public List<EntityChange> getRecentChanges(final int limit) {
        val snapshots = javers.findSnapshots(QueryBuilder.anyDomainObject().limit(Math.min(limit * SNAPSHOTS_PER_CHANGE, MAX_SNAPSHOTS)).build());
        return snapshots
            .stream()
            .filter(snapshot -> snapshot.getGlobalId() instanceof InstanceId)
            .limit(limit)
            .map(this::toChange)
            .toList();
    }

    private EntityChange toChange(final CdoSnapshot snapshot) {
        val instance = (InstanceId) snapshot.getGlobalId();
        val commit = snapshot.getCommitMetadata();
        val action = snapshot.getType() == SnapshotType.TERMINAL ? "deleted"
            : snapshot.getType() == SnapshotType.INITIAL ? "created" : "updated";
        return new EntityChange(String.valueOf(instance.getCdoId()), StringUtils.substringAfterLast(instance.getTypeName(), "."),
            nameOf(snapshot, instance), action, StringUtils.defaultIfBlank(commit.getAuthor(), SYSTEM_AUTHOR), commit.getCommitDateInstant());
    }

    private String nameOf(final CdoSnapshot snapshot, final InstanceId instance) {
        if (snapshot.getType() != SnapshotType.TERMINAL) {
            return Objects.toString(snapshot.getPropertyValue("name"), StringUtils.EMPTY);
        }
        return javers.findSnapshots(QueryBuilder.byInstanceId(instance.getCdoId(), instance.getTypeName()).limit(2).build())
            .stream()
            .filter(previous -> previous.getType() != SnapshotType.TERMINAL)
            .findFirst()
            .map(previous -> Objects.toString(previous.getPropertyValue("name"), StringUtils.EMPTY))
            .orElse(StringUtils.EMPTY);
    }

    @Override
    public List<HistoricalEntity> getHistory(final Object object) {
        val query = QueryBuilder.byInstance(object).build();
        val shadows = javers.findShadows(query);
        return shadows
            .stream()
            .filter(shadow -> shadow.get() != null)
            .map(shadow -> {
                val commit = shadow.getCommitMetadata();
                val date = commit.getCommitDate();
                val entity = Objects.requireNonNull((Serializable) shadow.get());
                return new HistoricalEntity(entity, shadow.getCommitId().value(), date,
                    StringUtils.defaultIfBlank(commit.getAuthor(), SYSTEM_AUTHOR));
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
