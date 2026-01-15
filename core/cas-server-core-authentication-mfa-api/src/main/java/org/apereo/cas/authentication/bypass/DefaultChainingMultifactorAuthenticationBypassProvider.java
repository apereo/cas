package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Class used to Chain multiple {@link MultifactorAuthenticationProviderBypassEvaluator} implementations.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Getter
@RequiredArgsConstructor
@ToString(of = "multifactorAuthenticationProviderBypassEvaluators")
public class DefaultChainingMultifactorAuthenticationBypassProvider implements ChainingMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = 2397239625822397286L;

    private final ConfigurableApplicationContext applicationContext;

    private final List<MultifactorAuthenticationProviderBypassEvaluator> multifactorAuthenticationProviderBypassEvaluators = new ArrayList<>();

    @Audit(action = AuditableActions.MULTIFACTOR_AUTHENTICATION_BYPASS,
        actionResolverName = AuditActionResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_RESOURCE_RESOLVER)
    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  @Nullable final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  @Nullable final HttpServletRequest request,
                                                                  @Nullable final Service service) {

        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .allMatch(bypass -> bypass.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request, service));
    }

    @Override
    public boolean isMultifactorAuthenticationBypassed(final Authentication authentication, final String requestedContext) {
        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .allMatch(bypass -> bypass.isMultifactorAuthenticationBypassed(authentication, requestedContext));
    }

    @Override
    public void forgetBypass(final Authentication authentication) {
        multifactorAuthenticationProviderBypassEvaluators
            .forEach(bypass -> bypass.forgetBypass(authentication));
    }

    @Override
    public void rememberBypass(final Authentication authentication, final MultifactorAuthenticationProvider provider) {
        multifactorAuthenticationProviderBypassEvaluators
            .forEach(bypass -> bypass.rememberBypass(authentication, provider));
    }

    @Override
    public String getProviderId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return getProviderId();
    }

    @Override
    public int size() {
        return multifactorAuthenticationProviderBypassEvaluators.size();
    }

    @Override
    public boolean isEmpty() {
        return multifactorAuthenticationProviderBypassEvaluators.isEmpty();
    }

    @Override
    public Optional<MultifactorAuthenticationProviderBypassEvaluator> belongsToMultifactorAuthenticationProvider(final String providerId) {
        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .filter(bypass -> bypass.belongsToMultifactorAuthenticationProvider(providerId).isPresent())
            .findFirst();
    }

    /**
     * Add bypass provider.
     *
     * @param bypass - the bypass provider
     */
    @Override
    public void addMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassEvaluator bypass) {
        if (BeanSupplier.isNotProxy(bypass) && !bypass.isEmpty()) {
            this.multifactorAuthenticationProviderBypassEvaluators.add(bypass);
        }
    }

    @Override
    public MultifactorAuthenticationProviderBypassEvaluator filterMultifactorAuthenticationProviderBypassEvaluatorsBy(final String providerId) {
        val chain = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .filter(bp -> bp.belongsToMultifactorAuthenticationProvider(providerId).isPresent())
            .forEach(chain::addMultifactorAuthenticationProviderBypassEvaluator);

        if (chain.isEmpty()) {
            return new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext);
        }
        return chain;
    }
}
