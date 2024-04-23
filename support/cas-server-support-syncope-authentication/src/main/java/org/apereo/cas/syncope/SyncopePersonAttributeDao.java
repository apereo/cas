package org.apereo.cas.syncope;

import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resolvedPeople, final PersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, List<Object>>();
        val results = syncopeSearch(uid);
        results.forEach(attributes::putAll);
        return new SimplePersonAttributes(uid, attributes);
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> map, final PersonAttributeDaoFilter filter,
                                           final Set<PersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map, filter), filter);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> map, final PersonAttributeDaoFilter filter,
        final Set<PersonAttributes> resolvedPeople) {
        return map.entrySet()
            .stream()
            .filter(e -> Objects.nonNull(e.getValue()))
            .filter(e -> !e.getValue().isEmpty())
            .filter(e -> properties.getSearchFilter().contains(e.getKey()))
            .findFirst()
            .map(e -> Set.of(getPerson(e.getValue().getFirst().toString(), resolvedPeople, filter)))
            .orElseGet(() -> new LinkedHashSet<>(0));
    }

    protected List<Map<String, List<Object>>> syncopeSearch(final String username) {
        return SyncopeUtils.syncopeUserSearch(properties, username);
    }
}
