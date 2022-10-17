package org.apereo.cas.syncope;

import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.NamedPersonImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link SyncopePersonAttributeDao}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class SyncopePersonAttributeDao extends BasePersonAttributeDao {

    private final SyncopePrincipalAttributesProperties properties;

    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> map) {
        return map.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, List<Object>>();
        val results = syncopeSearch(uid);
        results.forEach(attributes::putAll);
        return new NamedPersonImpl(uid, attributes);
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> map, final IPersonAttributeDaoFilter filter) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> map, final IPersonAttributeDaoFilter filter) {
        return map.entrySet()
            .stream()
            .filter(e -> Objects.nonNull(e.getValue()))
            .filter(e -> !e.getValue().isEmpty())
            .filter(e -> properties.getSearchFilter().contains(e.getKey()))
            .findFirst()
            .map(e -> Set.of(getPerson(e.getValue().get(0).toString(), filter)))
            .orElseGet(() -> new LinkedHashSet<>(0));
    }

    protected List<Map<String, List<Object>>> syncopeSearch(final String username) {
        return SyncopeUtils.syncopeUserSearch(properties, username);
    }
}
