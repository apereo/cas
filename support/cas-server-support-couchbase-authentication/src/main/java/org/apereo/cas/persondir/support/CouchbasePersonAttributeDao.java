package org.apereo.cas.persondir.support;

import com.couchbase.client.java.document.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.CouchbasePrincipalAttributesProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.util.CollectionUtils;
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

    @Override
    @SneakyThrows
    public IPersonAttributes getPerson(final String uid) {
        final var result = couchbase.query(couchbaseProperties.getUsernameAttribute(), uid);
        final Map<String, ?> attributes;
        if (result.allRows().isEmpty()) {
            LOGGER.debug("Couchbase query did not return any results/rows.");
            attributes = new LinkedHashMap<>();
        } else {
            attributes = result.allRows()
                .stream()
                .filter(row -> row.value().containsKey(couchbase.getBucket().name()))
                .filter(row -> {
                    final var value = (JsonObject) row.value().get(couchbase.getBucket().name());
                    return value.containsKey(couchbaseProperties.getUsernameAttribute());
                })
                .map(row -> (JsonObject) row.value().get(couchbase.getBucket().name()))
                .map(entity -> couchbase.collectAttributesFromEntity(entity, s -> !s.equals(couchbaseProperties.getUsernameAttribute())).entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return new CaseInsensitiveNamedPersonImpl(uid, stuffAttributesIntoList(attributes));
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> map) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map));
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> map) {
        final Set<IPersonAttributes> people = new LinkedHashSet();
        final var username = this.usernameAttributeProvider.getUsernameFromQuery(map);
        final var person = this.getPerson(username);
        if (person != null) {
            people.add(person);
        }

        return people;
    }

    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return new LinkedHashSet<>(0);
    }

    @Override
    public Set<String> getAvailableQueryAttributes() {
        return new LinkedHashSet<>(0);
    }

    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> personAttributesMap) {
        final Set<? extends Map.Entry<String, ?>> entries = personAttributesMap.entrySet();
        return entries.stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }
}
