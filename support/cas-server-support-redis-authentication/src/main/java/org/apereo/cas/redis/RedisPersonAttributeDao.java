package org.apereo.cas.redis;

import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.redis.core.CasRedisTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RedisPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public class RedisPersonAttributeDao extends BasePersonAttributeDao {
    @Setter
    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private final CasRedisTemplate redisTemplate;


    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resolvedPeople,
                                      final PersonAttributeDaoFilter filter) {
        val attributes = redisTemplate.opsForHash().entries(uid);
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

