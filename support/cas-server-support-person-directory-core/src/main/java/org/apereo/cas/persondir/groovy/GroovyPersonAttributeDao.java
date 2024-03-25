package org.apereo.cas.persondir.groovy;

import org.apereo.cas.authentication.attribute.AbstractDefaultAttributePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of the {@link PersonAttributeDao} that is able to resolve attributes
 * based on an external Groovy script, Groovy object, or Java object. Changes to the groovy script can be auto-detected
 * in certain use cases.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 **/
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class GroovyPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    private final PersonAttributeScriptDao groovyObject;

    private Set<String> possibleUserAttributeNames;
    private Set<String> availableQueryAttributes;

    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resultPeople, final PersonAttributeDaoFilter filter) {
        if (!this.isEnabled()) {
            return null;
        }
        val personAttributesMap = groovyObject.getAttributesForUser(Objects.requireNonNull(uid));
        if (personAttributesMap != null) {
            LOGGER.debug("Creating person attributes with the username [{}] and attributes [{}]", uid, personAttributesMap);
            val personAttributes = toMultivaluedMap(personAttributesMap);
            return new SimplePersonAttributes(uid, personAttributes);
        }
        return null;
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> attributes,
                                                                    final PersonAttributeDaoFilter filter,
                                                                    final Set<PersonAttributes> resultPeople) {
        val personAttributesMap = groovyObject.getPersonAttributesFromMultivaluedAttributes(attributes, resultPeople);
        if (personAttributesMap != null) {
            LOGGER.debug("Creating person attributes: [{}]", personAttributesMap);
            return Set.of(new SimplePersonAttributes(personAttributesMap));
        }
        return null;
    }
}
