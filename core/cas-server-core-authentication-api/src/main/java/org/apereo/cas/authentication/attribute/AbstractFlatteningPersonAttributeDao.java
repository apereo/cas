package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AbstractFlatteningPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class AbstractFlatteningPersonAttributeDao extends BasePersonAttributeDao {

    @Override
    public final Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                                 final PersonAttributeDaoFilter filter,
                                                 final Set<PersonAttributes> resultPeople) {
        var multivaluedSeed = toMultivaluedMap(query);
        return this.getPeopleWithMultivaluedAttributes(multivaluedSeed, filter, resultPeople);
    }
}
