package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Unchecked;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link ChainingSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class ChainingSurrogateAuthenticationService implements SurrogateAuthenticationService {
    private final List<SurrogateAuthenticationService> services;

    @Override
    public boolean canImpersonate(final String surrogate, final Principal principal, final Optional<Service> service) throws Throwable {
        return services.stream().anyMatch(Unchecked.predicate(impl -> impl.canImpersonate(surrogate, principal, service)));
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username) throws Throwable {
        return services.stream().map(Unchecked.function(impl -> impl.getImpersonationAccounts(username))).flatMap(Collection::stream).toList();
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal) throws Throwable {
        return services.stream().anyMatch(Unchecked.predicate(impl -> impl.isWildcardedAccount(surrogate, principal)));
    }

    @Override
    public boolean isWildcardedAccount(final Collection<String> accounts) {
        return services.stream().anyMatch(Unchecked.predicate(impl -> impl.isWildcardedAccount(accounts)));
    }
}
