package org.apereo.cas.persondir.groovy;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;

/**
 * Simplified DAO interface for use by Groovy scripts that provide user attributes.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 * @since 7.1.0
 */
@FunctionalInterface
public interface PersonAttributeScriptDao {
    /**
     * Gets attributes for user.
     *
     * @param username the username
     * @return the attributes for user
     */
    default Map<String, Object> getAttributesForUser(final String username) {
        return Map.of();
    }

    /**
     * Given a set of attributes, return additional attributes to add to the user's attributes.
     *
     * @param attributes   Map of attributes to query on
     * @param resultPeople the result people
     * @return A {@link Map} of attributes that match the query {@link Map}. If no matches are found an empty {@link Map} is returned. If the query could not be run null is returned.
     */
    Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> attributes,
                                                                           Set<PersonAttributes> resultPeople);
}
