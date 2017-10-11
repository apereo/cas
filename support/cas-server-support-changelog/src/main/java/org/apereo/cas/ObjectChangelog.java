package org.apereo.cas;

import org.javers.core.commit.Commit;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.shadow.Shadow;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link ObjectChangelog}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 5.2.0
 */
public interface ObjectChangelog<T> {

    /**
     * Delete commit.
     *
     * @param author the author
     * @param object the object
     * @return the commit
     */
    Commit delete(String author, T object);

    /**
     * Delete commit.
     *
     * @param author the author
     * @param object the object
     * @param branch the branch
     * @return the commit
     */
    Commit delete(String author, T object, String branch);

    /**
     * Delete commit.
     *
     * @param author the author
     * @param object the object
     * @param note   the note
     * @param branch the branch
     * @return the commit
     */
    Commit delete(String author, T object, String note, String branch);

    /**
     * Delete commit.
     *
     * @param author     the author
     * @param object     the object
     * @param properties the properties
     * @return the commit
     */
    Commit delete(String author, T object, Map<String, String> properties);

    /**
     * Find branches set.
     *
     * @param author the author
     * @return the set
     */
    Set<String> findBranches(String author);

    /**
     * Compare diff.
     *
     * @param oldObj the old obj
     * @param newObj the new obj
     * @return the diff
     */
    Diff compare(T oldObj, T newObj);

    /**
     * Merge commit.
     *
     * @param shadow the shadow
     * @param branch the branch
     * @return the commit
     */
    Commit merge(Shadow<T> shadow, String branch);

    /**
     * Commit commit.
     *
     * @param author the author
     * @param object the object
     * @param branch the branch
     * @param note   the notes
     * @return the commit
     */
    Commit commit(String author, T object, String branch, String note);

    /**
     * Commit commit.
     *
     * @param author the author
     * @param object the object
     * @return the commit
     */
    Commit commit(String author, T object);

    /**
     * Commit commit.
     *
     * @param author the author
     * @param branch the branch
     * @param object the object
     * @return the commit
     */
    Commit commit(String author, String branch, T object);

    /**
     * Commit commit.
     *
     * @param author     the author
     * @param object     the object
     * @param properties the properties
     * @return the commit
     */
    Commit commit(String author, T object, Map<String, String> properties);

    /**
     * Find changes list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @return the list
     */
    List<Change> findChanges(Object identifier, Class clazz);

    /**
     * Find changes list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param branch     the branch
     * @return the list
     */
    List<Change> findChanges(Object identifier, Class clazz, String author, String branch);

    /**
     * Find changes list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param fromDate   the from date
     * @param toDate     the to date
     * @param version    the version
     * @param skip       the skip
     * @param branch     the branch
     * @return the list
     */
    List<Change> findChanges(Object identifier, Class<T> clazz,
                             String author, LocalDate fromDate,
                             LocalDate toDate, int version,
                             int skip, String branch);

    /**
     * Find changes list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param fromDate   the from date
     * @param toDate     the to date
     * @param version    the version
     * @param skip       the skip
     * @return the list
     */
    List<Change> findChanges(Object identifier, Class<T> clazz,
                             String author, LocalDate fromDate,
                             LocalDate toDate, int version,
                             int skip);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param limit      the limit
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class clazz, int limit);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param limit      the limit
     * @param skip       the skip
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class clazz, int limit, int skip);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class clazz);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class clazz, String author);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param branch     the branch
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class clazz, String author, String branch);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param fromDate   the from date
     * @param toDate     the to date
     * @param version    the version
     * @param limit      the limit
     * @param skip       the skip
     * @param branch     the branch
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class<T> clazz,
                                    String author, LocalDate fromDate,
                                    LocalDate toDate, int version,
                                    int limit, int skip,
                                    String branch);

    /**
     * Find snapshots list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param fromDate   the from date
     * @param toDate     the to date
     * @param version    the version
     * @param limit      the limit
     * @param skip       the skip
     * @return the list
     */
    List<CdoSnapshot> findSnapshots(Object identifier, Class<T> clazz,
                                    String author, LocalDate fromDate,
                                    LocalDate toDate, int version,
                                    int limit, int skip);

    /**
     * Find shadows list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param commitId   the commit id
     * @return the list
     */
    List<Shadow<T>> findShadows(Object identifier, Class clazz, String author, String commitId);

    /**
     * Find shadows list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param commitId   the commit id
     * @return the list
     */
    List<Shadow<T>> findShadows(Object identifier, Class clazz, String commitId);

    /**
     * Find shadows list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @return the list
     */
    List<Shadow<T>> findShadows(Object identifier, Class clazz);

    /**
     * Find shadows list.
     *
     * @param identifier the identifier
     * @param clazz      the clazz
     * @param author     the author
     * @param fromDate   the from date
     * @param toDate     the to date
     * @param version    the version
     * @param commitId   the commit id
     * @return the list
     */
    List<Shadow<T>> findShadows(Object identifier, Class clazz,
                                String author, LocalDate fromDate,
                                LocalDate toDate, int version,
                                String commitId);
}
