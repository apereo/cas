package org.apereo.cas.persondir;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    class AttributeRepositoryQuery {
        private final AuthenticationHandler authenticationHandler;
        private final Principal principal;
        private final Service service;
        private final Set<String> activeRepositoryIds;
    }
}
