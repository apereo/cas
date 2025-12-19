package org.apereo.cas.scim.v2.persondirectory;

import module java.base;
import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 * This is {@link ScimPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Getter
@Setter
public class ScimPersonAttributeDao extends BasePersonAttributeDao {
    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private final ScimPersonAttributeService personAttributeService;
    
    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resolvedPeople,
                                      final PersonAttributeDaoFilter filter) {
        val attributes = personAttributeService.getPerson(uid);
        return new SimplePersonAttributes(uid, PersonAttributeDao.stuffAttributesIntoList(attributes));
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> map, final PersonAttributeDaoFilter filter,
                                           final Set<PersonAttributes> resolvedPeople) {
        return getPeopleWithMultivaluedAttributes(PersonAttributeDao.stuffAttributesIntoList(map), filter, resolvedPeople);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> map,
                                                                    final PersonAttributeDaoFilter filter,
                                                                    final Set<PersonAttributes> resolvedPeople) {
        val people = new LinkedHashSet<PersonAttributes>();
        val username = usernameAttributeProvider.getUsernameFromQuery(map);
        val person = getPerson(username, resolvedPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }
}
