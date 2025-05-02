package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.authentication.principal.merger.MultivaluedAttributeMerger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides a base set of implementations and properties for {@link PersonAttributeDao}
 * implementations that aggregate results from a sub List of {@link PersonAttributeDao}.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao implements AggregatingPersonAttributeDao {
    protected List<PersonAttributeDao> personAttributeDaos;

    /**
     * Strategy for merging together the results from successive PersonAttributeDaos.
     */
    protected AttributeMerger attributeMerger = new MultivaluedAttributeMerger();

    /**
     * True if we should catch, logger, and ignore Throwables propogated by
     * individual DAOs.
     */
    protected boolean recoverExceptions = true;

    /**
     * The Stop on success.
     */
    protected boolean stopOnSuccess;

    /**
     * Force all provided person attribute DAOs to
     * produce results, and otherwise, halt execution
     * if any of them cannot resolve the principal and return null.
     */
    protected boolean requireAll;

    @Override
    public String[] getId() {
        val ids = new ArrayList<String>();
        ids.add(getClass().getSimpleName());
        personAttributeDaos.forEach(dao -> ids.addAll(List.of(dao.getId())));
        return ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                    final PersonAttributeDaoFilter filter,
                                                                    final Set<PersonAttributes> resultPeople) {
        Set<PersonAttributes> results = null;
        var isFirstQuery = true;
        for (val currentlyConsidering : this.personAttributeDaos) {
            if (filter != null && !filter.choosePersonAttributeDao(currentlyConsidering)) {
                continue;
            }

            var handledException = false;
            Set<PersonAttributes> currentPeople = null;
            try {
                currentPeople = getAttributesFromDao(query, isFirstQuery, currentlyConsidering, results, filter);
                isFirstQuery = false;

                LOGGER.debug("Retrieved attributes=[{}] for query=[{}], isFirstQuery=[{}], currentlyConsidering=[{}], resultAttributes=[{}]",
                    currentPeople, query, isFirstQuery, currentlyConsidering, results);
            } catch (final Exception rte) {
                handledException = handledException || handleRuntimeException(currentlyConsidering, rte);
            }

            if (currentPeople != null) {
                if (results == null) {
                    results = new LinkedHashSet<>(currentPeople);
                } else {
                    results = attributeMerger.mergeResults(results, currentPeople);
                }
            } else if (this.requireAll) {
                LOGGER.debug("Attribute repository dao [{}] did not resolve a person "
                    + "and configuration requires all sources to produce valid results. ", currentlyConsidering);
                return null;
            }

            if (this.stopOnSuccess && !handledException) {
                LOGGER.debug("Successfully retrieved attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                break;
            }
        }

        if (results == null) {
            return null;
        }

        LOGGER.debug("Aggregated search results [{}] for query [{}]", results, query);
        return Set.copyOf(results);
    }

    private boolean handleRuntimeException(final PersonAttributeDao currentlyConsidering, final Exception ex) {
        if (this.recoverExceptions) {
            LOGGER.warn("Recovering From Exception thrown by [{}]", currentlyConsidering, ex);
            return true;
        }
        LOGGER.error("Failing From Exception thrown by [{}]", currentlyConsidering, ex);
        throw ex instanceof final RuntimeException rte ? rte : new RuntimeException(ex);
    }


    /**
     * Call to execute the appropriate query on the current {@link PersonAttributeDao}. Provides extra information
     * beyond the seed for the state of the query chain and previous results.
     *
     * @param seed                 The seed for the original query.
     * @param isFirstQuery         If this is the first query, this will stay true until a call to this method returns (does not throw an exception).
     * @param currentlyConsidering The {@link PersonAttributeDao} to execute the query on.
     * @param resultPeople         The Map of results from all previous queries, may be null.
     * @param filter               the filter
     * @return The results from the call to the DAO.
     */
    protected abstract Set<PersonAttributes> getAttributesFromDao(Map<String, List<Object>> seed,
                                                                  boolean isFirstQuery,
                                                                  PersonAttributeDao currentlyConsidering,
                                                                  Set<PersonAttributes> resultPeople,
                                                                  PersonAttributeDaoFilter filter);


    /**
     * Merges the results of calling {@link PersonAttributeDao#getPossibleUserAttributeNames(PersonAttributeDaoFilter)} on each child dao using
     * the configured {@link AttributeMerger#mergePossibleUserAttributeNames(Set, Set)}. If all children return null
     * this method returns null as well. If any child does not return null this method will not return null.
     *
     * @see PersonAttributeDao#getPossibleUserAttributeNames(PersonAttributeDaoFilter)
     */
    @Override
    public final Set<String> getPossibleUserAttributeNames(final PersonAttributeDaoFilter filter) {
        Set<String> attrNames = null;

        for (val currentDao : this.personAttributeDaos) {
            if (filter != null && !filter.choosePersonAttributeDao(currentDao)) {
                continue;
            }
            var handledException = false;
            Set<String> currentDaoAttrNames = null;
            try {
                currentDaoAttrNames = currentDao.getPossibleUserAttributeNames(filter);
                LOGGER.debug("Retrieved possible attribute names [{}] from [{}]", currentDaoAttrNames, currentDao);
            } catch (final Exception rte) {
                handledException = handledException || handleRuntimeException(currentDao, rte);
            }

            if (currentDaoAttrNames != null) {
                if (attrNames == null) {
                    attrNames = new LinkedHashSet<>();
                }
                attrNames = this.attributeMerger.mergePossibleUserAttributeNames(attrNames, currentDaoAttrNames);
            }

            if (this.stopOnSuccess && !handledException) {
                LOGGER.debug("Successfully retrieved possible user attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                break;
            }
        }
        LOGGER.debug("Aggregated possible attribute names [{}]", attrNames);
        if (attrNames == null) {
            return null;
        }
        return Set.copyOf(attrNames);
    }

    /**
     * Merges the results of calling {@link PersonAttributeDao#getAvailableQueryAttributes(PersonAttributeDaoFilter)} on each child dao using
     * the configured {@link AttributeMerger#mergeAvailableQueryAttributes(Set, Set)}. If all children return null this
     * method returns null as well. If any child does not return null this method will not return null.
     *
     * @see PersonAttributeDao#getAvailableQueryAttributes(PersonAttributeDaoFilter)
     */
    @JsonIgnore
    @Override
    public Set<String> getAvailableQueryAttributes(final PersonAttributeDaoFilter filter) {
        Set<String> queryAttrs = null;

        for (val currentDao : this.personAttributeDaos) {
            if (filter != null && !filter.choosePersonAttributeDao(currentDao)) {
                continue;
            }
            var handledException = false;
            Set<String> currentDaoQueryAttrs = null;
            try {
                currentDaoQueryAttrs = currentDao.getAvailableQueryAttributes(filter);
                LOGGER.debug("Retrieved possible query attributes [{}] from [{}]", currentDaoQueryAttrs, currentDao);
            } catch (final Exception rte) {
                handledException = handledException || handleRuntimeException(currentDao, rte);
            }

            if (currentDaoQueryAttrs != null) {
                if (queryAttrs == null) {
                    queryAttrs = new LinkedHashSet<>();
                }

                queryAttrs = this.attributeMerger.mergeAvailableQueryAttributes(queryAttrs, currentDaoQueryAttrs);
            }

            if (this.stopOnSuccess && !handledException) {
                LOGGER.debug("Successfully retrieved available query attributes from a child DAO and stopOnSuccess is true, stopping iteration of child DAOs");
                break;
            }
        }

        LOGGER.debug("Aggregated possible query attributes [{}]", queryAttrs);
        if (queryAttrs == null) {
            return null;
        }
        return Set.copyOf(queryAttrs);
    }
}
