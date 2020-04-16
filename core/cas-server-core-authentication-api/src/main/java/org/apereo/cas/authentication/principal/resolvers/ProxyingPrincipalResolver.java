package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Optional;

/**
 * Provides the most basic means of principal resolution by mapping
 * {@link Credential#getId()} onto
 * {@link Principal#getId()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@RequiredArgsConstructor
public class ProxyingPrincipalResolver implements PrincipalResolver {

    private final PrincipalFactory principalFactory;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal,
                             final Optional<AuthenticationHandler> handler) {
        return this.principalFactory.createPrincipal(credential.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getId() != null;
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
