package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link MockPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Setter
public class MockPersonAttributeDao implements PersonAttributeDao {
    private PersonAttributes person;

    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> set,
                                      final PersonAttributeDaoFilter filter) {
        return person;
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> map, final PersonAttributeDaoFilter filter,
                                           final Set<PersonAttributes> set) {
        return Set.of(person);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> query, final PersonAttributeDaoFilter filter,
        final Set<PersonAttributes> resultPeople) {
        return Set.of(person);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> query, final PersonAttributeDaoFilter filter) {
        return Set.of(person);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        return Set.of(person);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(
        final Map<String, List<Object>> query, final Set<PersonAttributes> resultPeople) {
        return Set.of(person);
    }

    @Override
    public Map<String, Object> getTags() {
        return Map.of();
    }

    @Override
    public int compareTo(@NonNull final PersonAttributeDao o) {
        return 0;
    }
}
