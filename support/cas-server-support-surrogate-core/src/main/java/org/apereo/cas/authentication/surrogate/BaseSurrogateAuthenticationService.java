package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.RegexUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Optional;

/**
 * This is {@link BaseSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSurrogateAuthenticationService implements SurrogateAuthenticationService {
    protected final ServicesManager servicesManager;
    protected final CasConfigurationProperties casProperties;
    protected final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;
    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public final boolean canImpersonate(final String surrogate, final Principal principal,
                                        final Optional<? extends Service> service) throws Throwable {
        val serviceAuthorized = isServiceAuthorizedForImpersonation(principal, service);
        return serviceAuthorized && (surrogate.equalsIgnoreCase(principal.getId())
            || isPrincipalAuthorizedForImpersonation(surrogate, principal, service)
            || isWildcardedAccount(surrogate, principal, service)
            || canImpersonateInternal(surrogate, principal, service));
    }

    protected boolean isServiceAuthorizedForImpersonation(final Principal principal,
                                                          final Optional<? extends Service> givenService) throws Throwable {
        if (givenService.isPresent()) {
            val service = givenService.get();
            val registeredService = servicesManager.findServiceBy(service);
            val accessGranted = principalAccessStrategyEnforcer.authorize(
                RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                    .registeredService(registeredService)
                    .principalId(principal.getId())
                    .principalAttributes(principal.getAttributes())
                    .service(service)
                    .applicationContext(applicationContext)
                    .build());
            return accessGranted
                && registeredService instanceof final WebBasedRegisteredService wbrs
                && wbrs.getSurrogatePolicy().isEnabled();
        }
        return true;
    }

    protected boolean isPrincipalAuthorizedForImpersonation(final String surrogate, final Principal principal,
                                                            final Optional<? extends Service> givenService) {
        val core = casProperties.getAuthn().getSurrogate().getCore();
        if (!core.getPrincipalAttributeNames().isEmpty() && !core.getPrincipalAttributeValues().isEmpty()) {
            return core.getPrincipalAttributeNames()
                .stream()
                .filter(name -> principal.getAttributes().containsKey(name))
                .anyMatch(name -> {
                    val attributeValues = principal.getAttributes().get(name);
                    return RegexUtils.findFirst(core.getPrincipalAttributeValues(), attributeValues).isPresent();
                });
        }
        return false;
    }

    protected abstract boolean canImpersonateInternal(String surrogate, Principal principal, Optional<? extends Service> service) throws Throwable;
}
