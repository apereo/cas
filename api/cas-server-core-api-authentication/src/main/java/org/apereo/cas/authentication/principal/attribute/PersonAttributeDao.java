package org.apereo.cas.authentication.principal.attribute;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Defines methods for finding a {@link PersonAttributes} or Set of IPersons based on a user ID or a Map of user attributes to
 * query with.
 *
 * @author andrew.petro @yale.edu
 * @author Eric Dalquist
 * @since 7.1.0
 */
public interface PersonAttributeDao extends Comparable<PersonAttributeDao>, Ordered {
    /**
     * Wildcard indicator.
     */
    String WILDCARD = "*";
    /**
     * Wildcard regex pattern.
     */
    Pattern WILDCARD_PATTERN = Pattern.compile(Pattern.quote(PersonAttributeDao.WILDCARD));

    /**
     * Searches for a single {@link PersonAttributes} using the specified uid (userName).<br>
     * <p>
     * This method returns according to the following rules:<br>
     * <ul>
     * <li>If the user exists and has attributes a populated {@link PersonAttributes} is returned.</li>
     * <li>If the user exists and has no attributes an {@link PersonAttributes} with an empty attributes Map is returned.</li>
     * <li>If the user doesn't exist {@code null} is returned.</li>
     * <li>If an error occurs while find the person an appropriate exception will be thrown.</li>
     * </ul>
     *
     * @param uid          The userName of the person to find.
     * @param resultPeople the result people
     * @param filter       the filter
     * @return The populated {@link PersonAttributes} for the specified uid, null if no person could be found for the uid.
     * @throws IllegalArgumentException If {@code uid} is {@code null.}
     */
    PersonAttributes getPerson(String uid,
                               Set<PersonAttributes> resultPeople,
                               PersonAttributeDaoFilter filter);

    /**
     * Gets person.
     *
     * @param uid the uid
     * @return the person
     */
    default PersonAttributes getPerson(final String uid) {
        return getPerson(uid, Set.of(), PersonAttributeDaoFilter.alwaysChoose());
    }

    /**
     * Gets person.
     *
     * @param uid          the uid
     * @param resultPeople the result people
     * @return the person
     */
    default PersonAttributes getPerson(final String uid,
                                       final Set<PersonAttributes> resultPeople) {
        return getPerson(uid, resultPeople, PersonAttributeDaoFilter.alwaysChoose());
    }

    /**
     * Searches for {@link PersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     *
     * @param query        A {@link Map} of name/value pair attributes to use in searching for {@link PersonAttributes}s
     * @param filter       the filter
     * @param resultPeople the result people
     * @return A {@link Set} of {@link PersonAttributes}s that match the query {@link Map}. If no matches are
     * found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If {@code query} is {@code null.}
     */
    Set<PersonAttributes> getPeople(Map<String, Object> query,
                                    PersonAttributeDaoFilter filter,
                                    Set<PersonAttributes> resultPeople);

    /**
     * Gets people.
     *
     * @param query the query
     * @return the people
     */
    default Set<PersonAttributes> getPeople(final Map<String, Object> query) {
        return getPeople(query, PersonAttributeDaoFilter.alwaysChoose(), Set.of());
    }

    /**
     * Gets people.
     *
     * @param query  the query
     * @param filter the filter
     * @return the people
     */
    default Set<PersonAttributes> getPeople(final Map<String, Object> query, final PersonAttributeDaoFilter filter) {
        return getPeople(query, filter, Set.of());
    }

    /**
     * Gets people.
     *
     * @param query        the query
     * @param resultPeople the result people
     * @return the people
     */
    default Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                            final Set<PersonAttributes> resultPeople) {
        return getPeople(query, PersonAttributeDaoFilter.alwaysChoose(), resultPeople);
    }

    /**
     * Searches for {@link PersonAttributes}s that match the set of attributes provided in the query {@link Map}. Each
     * implementation is free to define what qualifies as a 'match' is on its own. The provided query Map contains
     * String attribute names and single values which may be null.
     * <br>
     * If the implementation can not execute its query for an expected reason such as not enough information in the
     * query {@link Map} null should be returned. For unexpected problems throw an exception.
     *
     * @param query        A {@link Map} of name/value pair attributes to use in searching for {@link PersonAttributes}s
     * @param filter       the filter
     * @param resultPeople the result people from previous attempts, if any.
     * @return A {@link Set} of {@link PersonAttributes}s that match the query {@link Map}. If no matches are
     * found an empty {@link Set} is returned. If the query could not be run null is returned.
     * @throws IllegalArgumentException If {@code query} is {@code null.}
     */
    default Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final PersonAttributeDaoFilter filter,
                                                                     final Set<PersonAttributes> resultPeople) {
        return new LinkedHashSet<>();
    }

    /**
     * Gets people with multivalued attributes.
     *
     * @param query  the query
     * @param filter the filter
     * @return the people with multivalued attributes
     */
    default Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final PersonAttributeDaoFilter filter) {
        return getPeopleWithMultivaluedAttributes(query, filter, Set.of());
    }

    /**
     * Gets people with multivalued attributes.
     *
     * @param query the query
     * @return the people with multivalued attributes
     */
    default Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        return getPeopleWithMultivaluedAttributes(query, PersonAttributeDaoFilter.alwaysChoose(), Set.of());
    }

    /**
     * Gets people with multivalued attributes.
     *
     * @param query        the query
     * @param resultPeople the result people
     * @return the people with multivalued attributes
     */
    default Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final Set<PersonAttributes> resultPeople) {
        return getPeopleWithMultivaluedAttributes(query, PersonAttributeDaoFilter.alwaysChoose(), resultPeople);
    }

    /**
     * Gets a {@link Set} of attribute names that may be returned for an IPersonAttributes. The names returned represent all
     * possible attributes names for the {@link PersonAttributes} objects returned by the get methods. If the dao doesn't have a
     * way to know all possible attribute names this method should return {@code null}.
     * <br>
     * Returns an immutable {@link Set}.
     *
     * @param filter the filter
     * @return A {@link Set} of possible attribute names for user queries.
     */
    default Set<String> getPossibleUserAttributeNames(final PersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>();
    }

    /**
     * Gets a {@link Set} of attribute names that this implementation knows how to use in a query. The names returned
     * represent all possible names for query attributes for this implmenentation. If the dao doesn't have a way to know
     * all possible query attribute names this method should return {@code null}
     * <br>
     * Returns an immutable {@link Set}.
     *
     * @param filter the filter
     * @return The set of attributes that can be used to query for user ids in this dao, null if the set is unknown.
     */
    default Set<String> getAvailableQueryAttributes(final PersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>();
    }

    /**
     * Describes the order by which this DAO may be sorted
     * and put into an ordered collection.
     *
     * @return the numeric order.
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Gets the unique identifier for this dao.
     *
     * @return the id
     */
    default String[] getId() {
        return new String[]{this.getClass().getSimpleName()};
    }

    /**
     * Is this dao enabled?
     *
     * @return true/false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Gets properties assigned to this repository.
     * Properties are arbitrary tags and labels assigned
     * to a repository for follow-up processing.
     *
     * @return the properties
     */
    Map<String, Object> getTags();

    /**
     * Stuff attributes into list.
     *
     * @param personAttributesMap the person attributes map
     * @return the map
     */
    static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> personAttributesMap) {
        val personAttributes = new HashMap<String, List<Object>>();
        for (val stringObjectEntry : personAttributesMap.entrySet()) {
            val value = stringObjectEntry.getValue();
            if (value instanceof final List list && !list.isEmpty()) {
                personAttributes.put(stringObjectEntry.getKey(), (List<Object>) value);
            } else {
                personAttributes.put(stringObjectEntry.getKey(), new ArrayList<>(Collections.singletonList(value)));
            }
        }
        return personAttributes;
    }


    /**
     * Put tag into this DAO and override/remove existing tags by name.
     *
     * @param name  the name
     * @param value the value
     * @return the base person attribute dao
     */
    default PersonAttributeDao putTag(final String name, final Object value) {
        getTags().put(name, value);
        return this;
    }
    
    /**
     * Is disposable dao?.
     *
     * @return true/false
     */
    default boolean isDisposable() {
        return this instanceof DisposableBean
            && BooleanUtils.isTrue((Boolean) getTags().getOrDefault(DisposableBean.class.getName(), Boolean.FALSE));
    }

    /**
     * Mark disposable dao.
     *
     * @return the dao
     */
    @CanIgnoreReturnValue
    default PersonAttributeDao markDisposable() {
        putTag(DisposableBean.class.getName(), Boolean.TRUE);
        return this;
    }
}
