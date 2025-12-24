package org.apereo.cas.authentication.principal.resolvers;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Provides the most basic means of principal resolution.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@RequiredArgsConstructor
public class ProxyingPrincipalResolver implements PrincipalResolver {

    private final PrincipalFactory principalFactory;

    @Override
    public @Nullable Principal resolve(final Credential credential,
                                       final Optional<Principal> currentPrincipal,
                                       final Optional<AuthenticationHandler> handler,
                                       final Optional<Service> service) throws Throwable {
        val id = currentPrincipal.map(Principal::getId).orElseGet(credential::getId);
        val attributes = CollectionUtils.<String, List<Object>>wrap(
            HttpBasedServiceCredential.class.getName(), CollectionUtils.wrapList(credential.getId()));
        credential.getCredentialMetadata().putProperty(HttpBasedServiceCredential.class.getName(), credential.getId());
        return principalFactory.createPrincipal(id, attributes);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof HttpBasedServiceCredential && credential.getId() != null;
    }
}
