package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Looks up the user's attribute Map in the backingMap.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@NoArgsConstructor
@SuppressWarnings("NullAway.Init")
public class ComplexPersonAttributeDao extends AbstractQueryPersonAttributeDao<String> {
    @Getter
    private Map<String, Map<String, List<Object>>> backingMap = Map.of();
    private Set<String> possibleUserAttributeNames = Set.of();

    @Getter
    @Setter
    private String queryAttributeName;

    /**
     * The backing Map to use for queries, the outer map is keyed on the query attribute. The inner
     * Map is the set of user attributes to be returned for the query attribute.
     *
     * @param backingMap backing map
     */
    public void setBackingMap(final Map<String, Map<String, List<Object>>> backingMap) {
        this.backingMap = new LinkedHashMap<>(backingMap);
        possibleUserAttributeNames = backingMap.values().stream().map(Map::keySet)
            .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(@Nullable final PersonAttributeDaoFilter filter) {
        return Set.copyOf(this.possibleUserAttributeNames);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(@Nullable final PersonAttributeDaoFilter filter) {
        val usernameAttribute = getUsernameAttributeProvider().getUsernameAttribute();
        val list = new HashSet<String>();
        list.add(usernameAttribute);
        return list;
    }

    @Override
    protected @Nullable String appendAttributeToQuery(@Nullable final String queryBuilder,
                                                      @Nullable final String dataAttribute,
                                                      @Nullable final List<Object> queryValues) {
        if (queryBuilder != null) {
            return queryBuilder;
        }

        val keyAttributeName = queryAttributeName != null
            ? queryAttributeName
            : getUsernameAttributeProvider().getUsernameAttribute();

        if (keyAttributeName.equals(dataAttribute) && queryValues != null && !queryValues.isEmpty()) {
            return String.valueOf(queryValues.getFirst());
        }

        return null;
    }

    @Override
    protected @Nullable List<PersonAttributes> getPeopleForQuery(@Nullable final String seedValue, final String queryUserName) {
        if (seedValue != null && seedValue.contains(PersonAttributeDao.WILDCARD)) {
            val seedPattern = compilePattern(seedValue);
            val results = new ArrayList<PersonAttributes>();
            for (val attributesEntry : this.backingMap.entrySet()) {
                val attributesKey = attributesEntry.getKey();
                val keyMatcher = seedPattern.matcher(attributesKey);
                if (keyMatcher.matches()) {
                    val attributes = attributesEntry.getValue();
                    if (attributes != null) {
                        val person = createPerson(null, queryUserName, attributes);
                        results.add(person);
                    }
                }
            }
            if (results.isEmpty()) {
                return null;
            }
            return results;
        }
        val attributes = this.backingMap.get(seedValue);
        if (attributes == null) {
            return null;
        }

        val person = createPerson(seedValue, queryUserName, attributes);
        val list = new ArrayList<PersonAttributes>();
        list.add(person);
        return list;
    }

    private PersonAttributes createPerson(final @Nullable String seedValue, final String queryUserName,
                                          final Map<String, List<Object>> attributes) {
        val userNameAttribute = getConfiguredUserNameAttribute();
        if (isUserNameAttributeConfigured() && attributes.containsKey(userNameAttribute)) {
            return SimplePersonAttributes.fromAttribute(userNameAttribute, attributes);
        }
        if (queryUserName != null) {
            return new SimplePersonAttributes(queryUserName, attributes);
        }
        if (seedValue != null && userNameAttribute.equals(this.queryAttributeName)) {
            return new SimplePersonAttributes(seedValue, attributes);
        }
        return SimplePersonAttributes.fromAttribute(userNameAttribute, attributes);
    }
}

