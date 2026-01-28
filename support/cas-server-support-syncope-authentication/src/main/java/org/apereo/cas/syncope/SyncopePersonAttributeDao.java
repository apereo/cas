package org.apereo.cas.syncope;

import module java.base;
import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link SyncopePersonAttributeDao}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class SyncopePersonAttributeDao extends BasePersonAttributeDao {

    private final SyncopePrincipalAttributesProperties properties;

    @Override
    public PersonAttributes getPerson(final String uid,
                                      @Nullable final Set<PersonAttributes> resolvedPeople,
                                      @Nullable final PersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, List<Object>>();
        val results = syncopeUserSearch(uid);
        results.forEach(attributes::putAll);
        if (!results.isEmpty() && properties.isIncludeUserGroups()) {
            val groups = syncopeUserGroupsSearch(uid);
            groups.forEach(attributes::putAll);
        }
        return new SimplePersonAttributes(uid, attributes);
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> map,
                                           @Nullable final PersonAttributeDaoFilter filter,
                                           @Nullable final Set<PersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(PersonAttributeDao.stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> map,
        @Nullable final PersonAttributeDaoFilter filter,
        @Nullable final Set<PersonAttributes> resolvedPeople) {
        return map.entrySet()
            .stream()
            .filter(e -> !e.getValue().isEmpty())
            .filter(e -> properties.getSearchFilter().contains(e.getKey()))
            .findFirst()
            .map(e -> Set.of(getPerson(e.getValue().getFirst().toString(), resolvedPeople, filter)))
            .orElseGet(LinkedHashSet::new);
    }

    protected List<Map<String, List<Object>>> syncopeUserSearch(final String username) {
        return SyncopeUtils.syncopeUserSearch(properties, username);
    }

    protected List<Map<String, List<Object>>> syncopeUserGroupsSearch(final String username) {
        return SyncopeUtils.syncopeUserGroupsSearch(properties, username);
    }

    
}
