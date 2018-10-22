package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SurrogatePrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    private static final long serialVersionUID = -3112906686072339162L;

    private final PrincipalFactory principalFactory;

    public SurrogatePrincipalElectionStrategy() {
        this(PrincipalFactoryUtils.newPrincipalFactory());
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, Object> principalAttributes) {
        final Principal principal = getPrincipalFromAuthentication(authentications);
        final Principal finalPrincipal;

        if (principal instanceof SurrogatePrincipal) {
            final Principal surrogatePrincipal = ((SurrogatePrincipal) principal).getSurrogate();
            LOGGER.debug("Principal [{}] indicates a surrogate", surrogatePrincipal);
            finalPrincipal = this.principalFactory.createPrincipal(surrogatePrincipal.getId(), surrogatePrincipal.getAttributes());
        } else {
            finalPrincipal = this.principalFactory.createPrincipal(principal.getId(), principalAttributes);
        }

        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }

    /**
     * Gets principal from authentication.
     *
     * @param authentications the authentications
     * @return the principal from authentication
     */
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        final Optional<SurrogatePrincipal> result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            return result.get().getSurrogate();
        }
        return super.getPrincipalFromAuthentication(authentications);
    }
}
