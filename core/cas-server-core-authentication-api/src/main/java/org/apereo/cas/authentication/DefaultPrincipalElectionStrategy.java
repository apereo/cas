package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultPrincipalElectionStrategy} that selects the primary principal
 * to be the first principal in the chain of authentication history.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultPrincipalElectionStrategy implements PrincipalElectionStrategy {

    private static final long serialVersionUID = 6704726217030836315L;

    private final PrincipalFactory principalFactory;

    public DefaultPrincipalElectionStrategy() {
        this(PrincipalFactoryUtils.newPrincipalFactory());
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, List<Object>> principalAttributes) {
        val principal = getPrincipalFromAuthentication(authentications);
        val attributes = getPrincipalAttributesForPrincipal(principal, principalAttributes);
        val finalPrincipal = principalFactory.createPrincipal(principal.getId(), attributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }

    /**
     * Gets principal attributes for principal.
     *
     * @param principal           the principal
     * @param principalAttributes the principal attributes
     * @return the principal attributes for principal
     */
    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Principal principal, final Map<String, List<Object>> principalAttributes) {
        return principalAttributes;
    }

    /**
     * Gets principal from authentication.
     *
     * @param authentications the authentications
     * @return the principal from authentication
     */
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        return authentications.iterator().next().getPrincipal();
    }
}
