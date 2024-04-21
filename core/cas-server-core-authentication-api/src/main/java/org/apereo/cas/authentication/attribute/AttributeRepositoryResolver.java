package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.services.RegisteredService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
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

    @SuperBuilder
    @With
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor(force = true)
    @RequiredArgsConstructor
    class AttributeRepositoryQuery {
        private AuthenticationHandler authenticationHandler;
        private final Principal principal;
        private Service service;
        private final Set<String> activeRepositoryIds;
        private RegisteredService registeredService;
    }

    /**
     * All repositories attribute repository resolver.
     *
     * @return the attribute repository resolver
     */
    static AttributeRepositoryResolver allAttributeRepositories() {
        return query -> Set.of(PersonAttributeDao.WILDCARD);
    }
}
