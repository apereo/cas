package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import java.util.Set;

/**
 * This is {@link AttributeRepositoryResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface AttributeRepositoryResolver {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "attributeRepositoryResolver";

    /**
     * Resolve attribute repository ids that should be used for attribute fetching..
     *
     * @param query the query
     * @return the set
     */
    Set<String> resolve(AttributeRepositoryQuery query);


    /**
     * All repositories attribute repository resolver.
     *
     * @return the attribute repository resolver
     */
    static AttributeRepositoryResolver allAttributeRepositories() {
        return query -> Set.of(PersonAttributeDao.WILDCARD);
    }
}
