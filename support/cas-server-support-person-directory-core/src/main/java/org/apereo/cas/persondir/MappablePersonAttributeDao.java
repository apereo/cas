package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.authentication.attribute.AbstractQueryPersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.val;


/**
 * Looks up the user's attribute Map in the backingMap.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class MappablePersonAttributeDao extends AbstractQueryPersonAttributeDao<String> {
    @Getter
    private Map<String, Map<String, List<Object>>> backingMap = new HashMap<>();
    private Set<String> possibleUserAttributeNames = new HashSet<>();
    @Getter
    private String queryAttributeName;

    /**
     * Sets backing map.
     *
     * @param backingMap the backing map
     */
    public void setBackingMap(final Map<String, Map<String, List<Object>>> backingMap) {
        this.backingMap = new LinkedHashMap<>(backingMap);
        this.initializePossibleAttributeNames();
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final PersonAttributeDaoFilter filter) {
        return Set.copyOf(this.possibleUserAttributeNames);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final PersonAttributeDaoFilter filter) {
        val usernameAttributeProvider = this.getUsernameAttributeProvider();
        val usernameAttribute = usernameAttributeProvider.getUsernameAttribute();
        val list = new HashSet<String>();
        list.add(usernameAttribute);
        return list;
    }

    @Override
    protected String appendAttributeToQuery(final String queryBuilder, final String dataAttribute, final List<Object> queryValues) {
        if (queryBuilder != null) {
            return queryBuilder;
        }
        val keyAttributeName = this.queryAttributeName != null
            ? this.queryAttributeName
            : getUsernameAttributeProvider().getUsernameAttribute();
        if (keyAttributeName.equals(dataAttribute)) {
            return String.valueOf(queryValues.getFirst());
        }
        return null;
    }

    @Override
    protected List<PersonAttributes> getPeopleForQuery(final String seedValue, final String queryUserName) {
        if (seedValue != null && seedValue.contains(PersonAttributeDao.WILDCARD)) {
            var seedPattern = compilePattern(seedValue);
            val results = new ArrayList<PersonAttributes>();
            for (val attributesEntry : this.backingMap.entrySet()) {
                val attributesKey = attributesEntry.getKey();
                val keyMatcher = seedPattern.matcher(attributesKey);
                if (keyMatcher.matches()) {
                    val attributes = attributesEntry.getValue();
                    if (attributes != null) {
                        val person = this.createPerson(null, queryUserName, attributes);
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

    private PersonAttributes createPerson(final String seedValue, final String queryUserName,
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

    private void initializePossibleAttributeNames() {
        this.possibleUserAttributeNames = backingMap.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

