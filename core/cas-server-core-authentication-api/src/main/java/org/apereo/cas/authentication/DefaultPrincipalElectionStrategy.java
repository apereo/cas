package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import java.util.Collection;
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
                              final Map<String, Object> principalAttributes) {
        final var principal = getPrincipalFromAuthentication(authentications);
        final var finalPrincipal = this.principalFactory.createPrincipal(principal.getId(), principalAttributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
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
