package org.apereo.cas.persondir.support;

import org.apereo.cas.configuration.model.core.authentication.CouchbasePrincipalAttributesProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CouchbasePersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class CouchbasePersonAttributeDao extends BasePersonAttributeDao {
    private final IUsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private final CouchbasePrincipalAttributesProperties couchbaseProperties;

    private final CouchbaseClientFactory couchbase;

    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> personAttributesMap) {
        val entries = (Set<? extends Map.Entry<String, ?>>) personAttributesMap.entrySet();
        return entries.stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }

    @Override
    @SneakyThrows
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        val query = String.format("%s = '%s'", couchbaseProperties.getUsernameAttribute(), uid);
        val result = couchbase.select(query);
        val attributes = new LinkedHashMap<String, Object>();
        if (result.rowsAsObject().isEmpty()) {
            LOGGER.debug("Couchbase query did not return any results/rows.");
        } else {
            val rows = result.rowsAsObject();
            attributes.putAll(rows.stream()
                .filter(row -> row.containsKey(couchbase.getBucket()))
                .map(row -> {
                    val document = row.getObject(couchbase.getBucket());
                    val results = CouchbaseClientFactory.collectAttributesFromEntity(document, s -> true);
                    return results.entrySet();
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoList(attributes));
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> map, final IPersonAttributeDaoFilter filter) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> map, final IPersonAttributeDaoFilter filter) {
        val people = new LinkedHashSet<IPersonAttributes>(map.size());
        val username = this.usernameAttributeProvider.getUsernameFromQuery(map);
        val person = this.getPerson(username, filter);
        if (person != null) {
            people.add(person);
        }

        return people;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }
}
