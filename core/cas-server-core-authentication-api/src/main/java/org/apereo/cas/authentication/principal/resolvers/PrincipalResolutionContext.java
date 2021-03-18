package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.merger.IAttributeMerger;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link PrincipalResolutionContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
public class PrincipalResolutionContext {
    /**
     * Repository of principal attributes to be retrieved.
     */
    private final IPersonAttributeDao attributeRepository;

    /**
     * Factory to create the principal type.
     **/
    private final PrincipalFactory principalFactory;

    /**
     * return null if no attributes are found.
     */
    private final boolean returnNullIfNoAttributes;

    /**
     * Transform principal name.
     */
    private final PrincipalNameTransformer principalNameTransformer;

    /**
     * Optional principal attribute name.
     */
    private final String principalAttributeNames;

    /**
     * Use the current principal id for extraction.
     */
    private final boolean useCurrentPrincipalId;

    /**
     * Whether attributes should be fetched from attribute repositories.
     */
    private final boolean resolveAttributes;

    /**
     * Active attribute repositories ids for this resolver
     * to use for attribute resolution.
     */
    @Builder.Default
    private final Set<String> activeAttributeRepositoryIdentifiers = new HashSet<>();

    private final IAttributeMerger attributeMerger;
}
