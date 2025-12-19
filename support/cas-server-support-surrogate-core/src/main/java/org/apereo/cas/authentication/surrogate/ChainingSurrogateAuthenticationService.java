package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceSurrogatePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link ChainingSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class ChainingSurrogateAuthenticationService implements SurrogateAuthenticationService {
    private final List<SurrogateAuthenticationService> surrogateServices;
    private final ServicesManager servicesManager;

    @Override
    public boolean canImpersonate(final String surrogate, final Principal principal, final Optional<? extends Service> givenService) {
        return isImpersonationAllowedFor(givenService)
            && surrogateServices.stream().anyMatch(Unchecked.predicate(impl -> impl.canImpersonate(surrogate, principal, givenService)));
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> givenService) {
        return isImpersonationAllowedFor(givenService)
            ? surrogateServices.stream().map(Unchecked.function(impl -> impl.getImpersonationAccounts(username, givenService))).flatMap(Collection::stream).toList()
            : new ArrayList<>();
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal, final Optional<? extends Service> givenService) {
        return isImpersonationAllowedFor(givenService)
            && surrogateServices.stream().anyMatch(Unchecked.predicate(impl -> impl.isWildcardedAccount(surrogate, principal, givenService)));
    }

    @Override
    public boolean isWildcardedAccount(final Collection<String> accounts, final Optional<? extends Service> givenService) {
        return isImpersonationAllowedFor(givenService)
            && surrogateServices.stream().anyMatch(Unchecked.predicate(impl -> impl.isWildcardedAccount(accounts, givenService)));
    }

    protected boolean isImpersonationAllowedFor(final Optional<? extends Service> givenService) {
        val surrogatePolicyResult = givenService
            .map(servicesManager::findServiceBy)
            .filter(WebBasedRegisteredService.class::isInstance)
            .map(WebBasedRegisteredService.class::cast)
            .filter(service -> Objects.nonNull(service.getSurrogatePolicy()))
            .map(WebBasedRegisteredService::getSurrogatePolicy)
            .stream()
            .findFirst();
        return surrogatePolicyResult
            .map(RegisteredServiceSurrogatePolicy::isEnabled)
            .orElse(Boolean.TRUE);
    }
}
