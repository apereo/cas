package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is {@link AbstractDefaultAttributePersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractDefaultAttributePersonAttributeDao extends AbstractFlatteningPersonAttributeDao {
    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();
    
    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resultPeople, final PersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        val seed = toSeedMap(uid);
        val people = getPeopleWithMultivaluedAttributes(seed, filter, resultPeople);
        var person = getSinglePerson(people);
        if (person == null) {
            return null;
        }
        if (person.getName() == null) {
            person = new SimplePersonAttributes(uid, person.getAttributes());
        }

        return person;
    }

    protected Map<String, List<Object>> toSeedMap(final String username) {
        val values = Collections.singletonList((Object) username);
        val usernameAttribute = this.usernameAttributeProvider.getUsernameAttribute();
        val seed = Map.of(usernameAttribute, values);
        LOGGER.debug("Created seed map [{}] for username [{}]", seed, username);
        return seed;
    }
}
