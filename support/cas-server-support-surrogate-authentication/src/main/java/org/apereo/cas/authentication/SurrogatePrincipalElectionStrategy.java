package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

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
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    private static final long serialVersionUID = -3112906686072339162L;

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, Object> principalAttributes) {

        final Optional<SurrogatePrincipal> result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            final Principal surrogatePrincipal = result.get().getSurrogate();
            LOGGER.debug("Nominated [{}] as the surrogate principal", surrogatePrincipal);
            return surrogatePrincipal;
        }
        return super.nominate(authentications, principalAttributes);
    }

}
