package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Provides the ability to define static attributes and values.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class StubPersonAttributeDao extends BasePersonAttributeDao {
    private PersonAttributes backingPerson;

    public StubPersonAttributeDao() {
        this.backingPerson = new SimplePersonAttributes();
    }

    public StubPersonAttributeDao(final Map<String, List<Object>> backingMap) {
        this.backingPerson = new SimplePersonAttributes(backingMap);
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(@Nullable final PersonAttributeDaoFilter filter) {
        return Set.copyOf(this.backingPerson.getAttributes().keySet());
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                    @Nullable final PersonAttributeDaoFilter filter,
                                                                    @Nullable final Set<PersonAttributes> resultPeople) {
        val list = query.get("username");
        if (list != null && !list.isEmpty()) {
            val newMap = new HashMap(this.getBackingMap());
            newMap.put("username", list);
            return Set.of(new SimplePersonAttributes(newMap));
        }
        return Set.of(backingPerson);
    }

    @Override
    public @Nullable PersonAttributes getPerson(final String uid,
                                                final Set<PersonAttributes> resultPeople,
                                                final PersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        return this.backingPerson;
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                           final PersonAttributeDaoFilter filter,
                                           final Set<PersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(PersonAttributeDao.stuffAttributesIntoList(query), filter, resolvedPeople);
    }

    public void setBackingMap(final Map<String, List<Object>> backingMap) {
        this.backingPerson = new SimplePersonAttributes(backingMap);
    }

    
    public Map<String, List<Object>> getBackingMap() {
        return new HashMap(this.backingPerson.getAttributes());
    }
}
