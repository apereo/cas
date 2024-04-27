package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import java.util.Optional;

/**
 * Provides the most basic means of principal resolution.
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@RequiredArgsConstructor
public class ProxyingPrincipalResolver implements PrincipalResolver {

    private final PrincipalFactory principalFactory;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal,
                             final Optional<AuthenticationHandler> handler, final Optional<Service> service) throws Throwable {
        val id = currentPrincipal.map(Principal::getId).orElseGet(credential::getId);
        return principalFactory.createPrincipal(id);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getId() != null;
    }
}
