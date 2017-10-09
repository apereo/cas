package org.apereo.cas;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.CollectionUtils;
import org.javers.core.Javers;
import org.javers.core.commit.Commit;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultObjectChangelog}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultObjectChangelog<T> implements ObjectChangelog<T> {
    private final Javers javers;

    public DefaultObjectChangelog(final Javers javers) {
        this.javers = javers;
    }

    @Override
    public Commit delete(final String author, final T object) {
        return delete(author, object, "master");
    }

    @Override
    public Commit delete(final String author, final T object, final String branch) {
        return delete(author, object, "Deleted", branch);
    }

    @Override
    public Commit delete(final String author, final T object, final String note, final String branch) {
        return delete(author, object, CollectionUtils.wrap("note", note, "branch", branch));
    }

    @Override
    public Commit delete(final String author, final T object, final Map<String, String> properties) {
        return javers.commitShallowDelete(author, object, properties);
    }

    @Override
    public Set<String> findBranches(final String author) {
        final List<Change> changes = findChanges(null, null, author, null);
        return changes.stream()
                .filter(c -> c.getCommitMetadata().isPresent())
                .map(c -> {
                    final Map<String, String> props = c.getCommitMetadata().get().getProperties();
                    return props.containsKey("branch")
                            ? props.get("branch")
                            : null;
                })
                .filter(Objects::nonNull)
                .sorted()
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public Diff compare(final T oldObj, final T newObj) {
        return javers.compare(newObj, newObj);
    }

    @Override
    public Commit merge(final Shadow<T> shadow, final String branch) {
        final T object = shadow.get();
        final CommitMetadata md = shadow.getCommitMetadata();
        final Map<String, String> props = md.getProperties();
        props.put("branch", branch);
        return commit(md.getAuthor(), object, props);
    }

    @Override
    public Commit commit(final String author, final T object, final String branch, final String note) {
        return commit(author, object, CollectionUtils.wrap("note", note, "branch", branch));
    }

    @Override
    public Commit commit(final String author, final T object) {
        return commit(author, "master", object);
    }

    @Override
    public Commit commit(final String author, final String branch, final T object) {
        return commit(author, object, branch, null);
    }

    @Override
    public Commit commit(final String author, final T object, final Map<String, String> properties) {
        return javers.commit(author, object, properties);
    }

    @Override
    public List<Change> findChanges(final Object identifier, final Class clazz) {
        return findChanges(identifier, clazz, null, null, null, 0, 0);
    }

    @Override
    public List<Change> findChanges(final Object identifier, final Class clazz, final String author, final String branch) {
        return findChanges(identifier, clazz, author, null, null, 0, 0, branch);
    }

    @Override
    public List<Change> findChanges(final Object identifier, final Class<T> clazz,
                                    final String author, final LocalDate fromDate,
                                    final LocalDate toDate, final int version,
                                    final int skip, final String branch) {
        final JqlQuery query = buildQuery(identifier, clazz, author, fromDate,
                toDate, version, 0, skip, new HashMap<>(),
                false, null, branch);
        return javers.findChanges(query);
    }

    @Override
    public List<Change> findChanges(final Object identifier, final Class<T> clazz,
                                    final String author, final LocalDate fromDate,
                                    final LocalDate toDate, final int version,
                                    final int skip) {
        return findChanges(identifier, clazz, author, fromDate, toDate, version, skip, null);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class clazz, final int limit) {
        return findSnapshots(identifier, clazz, null, null, null, 0, limit, 0);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class clazz, final int limit, final int skip) {
        return findSnapshots(identifier, clazz, null, null, null, 0, limit, skip);
    }


    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class clazz) {
        return findSnapshots(identifier, clazz, null, null, null, 0, 0, 0);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class clazz, final String author) {
        return findSnapshots(identifier, clazz, author, null, null, 0, 0, 0);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class clazz, final String author, final String branch) {
        return findSnapshots(identifier, clazz, author, null, null, 0, 0, 0);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class<T> clazz,
                                           final String author, final LocalDate fromDate,
                                           final LocalDate toDate, final int version,
                                           final int limit, final int skip,
                                           final String branch) {
        final JqlQuery query = buildQuery(identifier, clazz, author, fromDate,
                toDate, version, limit, skip, new HashMap<>(),
                false, null, branch);
        return javers.findSnapshots(query);
    }

    @Override
    public List<CdoSnapshot> findSnapshots(final Object identifier, final Class<T> clazz,
                                           final String author, final LocalDate fromDate,
                                           final LocalDate toDate, final int version,
                                           final int limit, final int skip) {
        return findSnapshots(identifier, clazz, author, fromDate, toDate, version, limit, skip, null);
    }

    @Override
    public List<Shadow<T>> findShadows(final Object identifier, final Class clazz, final String author, final String commitId) {
        return findShadows(identifier, clazz, author, null, null, 0, commitId);
    }

    @Override
    public List<Shadow<T>> findShadows(final Object identifier, final Class clazz, final String commitId) {
        return findShadows(identifier, clazz, null, commitId);
    }

    @Override
    public List<Shadow<T>> findShadows(final Object identifier, final Class clazz) {
        return findShadows(identifier, clazz, null, null, null, 0, null);
    }

    @Override
    public List<Shadow<T>> findShadows(final Object identifier, final Class clazz,
                                       final String author, final LocalDate fromDate,
                                       final LocalDate toDate, final int version,
                                       final String commitId) {
        final JqlQuery query = buildQuery(identifier, clazz, author, fromDate,
                toDate, version, 0, 0, new HashMap<>(),
                false, commitId, "master");
        return javers.findShadows(query);
    }

    /**
     * Build query jql query.
     *
     * @param identifier           the identifier
     * @param clazz                the clazz
     * @param author               the author
     * @param fromDate             the from date
     * @param toDate               the to date
     * @param version              the version
     * @param limit                the limit. Optional parameter for all queries, default limit is 100. It simply limits the number of most recent snapshots
     *                             to be read from repository. Always choose reasonable limits to improve performance of your queries and to save server heap
     *                             size.
     * @param skip                 the skip. This is an optional parameter for all queries (the default skip is 0). It defines the offset of the first (most
     *                             recent) snapshot that it should fetch from a repository. Skip and limit parameters can be useful for implementing pagination.
     * @param properties           the properties
     * @param withNewObjectChanges This filter only affects queries for changes, by default it’s disabled. When enabled, a query produces additional
     *                             changes or initial snapshots. An initial snapshot is taken when an object is committed to repository for the first time.
     *                             With this filter, you can query for the initial state of an object. It’s represented as a NewObject change, followed by a
     *                             list of property changes from null to something.
     * @param commitId             the commit id filter
     * @param branch               the branch filter
     * @return the jql query
     */
    protected JqlQuery buildQuery(final Object identifier, final Class<T> clazz,
                                  final String author,
                                  final LocalDate fromDate,
                                  final LocalDate toDate, final int version,
                                  final int limit, final int skip,
                                  final Map<String, String> properties,
                                  final boolean withNewObjectChanges,
                                  final String commitId,
                                  final String branch) {
        QueryBuilder queryBuilder = null;

        if (identifier != null && clazz != null) {
            queryBuilder = QueryBuilder.byInstanceId(identifier, clazz).withChildValueObjects();
        } else if (identifier == null && clazz != null) {
            queryBuilder = QueryBuilder.byClass(clazz).withChildValueObjects();
        } else if (identifier != null && clazz == null) {
            queryBuilder = QueryBuilder.byInstance(clazz).withChildValueObjects();
        } else if (identifier == null && clazz == null) {
            queryBuilder = QueryBuilder.anyDomainObject();
        }

        queryBuilder = queryBuilder.withNewObjectChanges(withNewObjectChanges);

        if (StringUtils.isNotBlank(author)) {
            queryBuilder = queryBuilder.byAuthor(author);
        }
        if (fromDate != null) {
            queryBuilder = queryBuilder.from(fromDate);
        }
        if (toDate != null) {
            queryBuilder = queryBuilder.to(toDate);
        }
        if (limit > 0) {
            queryBuilder = queryBuilder.limit(limit);
        }
        if (skip > 0) {
            queryBuilder = queryBuilder.skip(skip);
        }
        if (version > 0) {
            queryBuilder = queryBuilder.withVersion(version);
        }
        if (StringUtils.isNotBlank(commitId)) {
            queryBuilder = queryBuilder.withCommitId(CommitId.valueOf(commitId));
        }
        if (StringUtils.isNotBlank(branch)) {
            queryBuilder = queryBuilder.withCommitProperty("branch", branch);
        }
        if (properties != null) {
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                queryBuilder = queryBuilder.withCommitProperty(entry.getKey(), entry.getValue());
            }
        }
        return queryBuilder.build();
    }
}
