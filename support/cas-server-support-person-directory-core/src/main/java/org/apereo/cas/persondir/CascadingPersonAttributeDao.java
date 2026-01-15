package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.authentication.attribute.AbstractAggregatingDefaultQueryPersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.merger.ReplacingAttributeAdder;
import lombok.Getter;
import lombok.Setter;
import lombok.val;


/**
 * This {@link PersonAttributeDao}
 * implementation iterates through an ordered {@link List} of
 * {@link PersonAttributeDao} impls
 * when getting user attributes.
 * <br>
 * The first DAO is queried using the seed {@link Map} passed to this class. The results
 * of the query are merged into a general result map. After the first DAO this general
 * result map used as the query seed for each DAO and each DAO's results are merged into it.
 * <ul>
 * <li>If the first DAO returned null/no results and {@code stopIfFirstDaoReturnsNull}=true, no subsequent DAO is
 * called and null is the final result.</li>
 * <li>If the first DAO returned null/no results and {@code stopIfFirstDaoReturnsNull}=false, each subsequent DAO is
 * called and the first that returns a result is used as the seed to the remaining child DAOs.  This is the default
 * to support legacy behavior.</li>
 * </ul>
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Getter
@Setter
public class CascadingPersonAttributeDao extends AbstractAggregatingDefaultQueryPersonAttributeDao {

    /**
     * Set to true to not invoke child DAOs if first DAO returns null or no results.  Default: false
     * to support legacy behavior.
     *
     * @since 1.6.0
     */
    private boolean stopIfFirstDaoReturnsNull;

    private boolean addOriginalAttributesToQuery;
    
    public CascadingPersonAttributeDao() {
        setAttributeMerger(new ReplacingAttributeAdder());
    }

    /**
     * If this is the first call, or there are no results in the resultPeople Set and stopIfFirstDaoReturnsNull=false,
     * the seed map is used. If not the attributes of the first user in the resultPeople Set are used for each child
     * dao.  If stopIfFirstDaoReturnsNull=true and the first query returned no results in the resultPeopleSet,
     * return null.
     */
    @Override
    protected Set<PersonAttributes> getAttributesFromDao(final Map<String, List<Object>> seed, final boolean isFirstQuery,
                                                         final PersonAttributeDao currentlyConsidering,
                                                         final Set<PersonAttributes> resultPeople,
                                                         final PersonAttributeDaoFilter filter) {
        if (isFirstQuery || (!stopIfFirstDaoReturnsNull && (resultPeople == null || resultPeople.isEmpty()))) {
            return currentlyConsidering.getPeopleWithMultivaluedAttributes(seed, filter, resultPeople);
        }
        if (stopIfFirstDaoReturnsNull && !isFirstQuery && (resultPeople == null || resultPeople.isEmpty())) {
            return null;
        }

        Set<PersonAttributes> mergedPeopleResults = null;
        for (val person : resultPeople) {
            val queryAttributes = new LinkedHashMap<String, List<Object>>();
            val userName = person.getName();
            if (userName != null) {
                val userNameMap = this.toSeedMap(userName);
                queryAttributes.putAll(userNameMap);
            }

            val personAttributes = person.getAttributes();
            queryAttributes.putAll(personAttributes);

            if (this.addOriginalAttributesToQuery) {
                queryAttributes.putAll(seed);
            }

            val newResults = currentlyConsidering.getPeopleWithMultivaluedAttributes(queryAttributes, filter, resultPeople);
            if (newResults != null) {
                if (mergedPeopleResults == null) {
                    mergedPeopleResults = new LinkedHashSet<>(newResults);
                } else {
                    mergedPeopleResults = attributeMerger.mergeResults(mergedPeopleResults, newResults);
                }
            }
        }

        return mergedPeopleResults;
    }
}
