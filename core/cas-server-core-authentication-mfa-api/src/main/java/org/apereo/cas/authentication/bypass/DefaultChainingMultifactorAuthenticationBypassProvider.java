package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
                                                                                               
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class used to Chain multiple {@link MultifactorAuthenticationProviderBypassEvaluator} implementations.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Getter
@NoArgsConstructor
public class DefaultChainingMultifactorAuthenticationBypassProvider implements ChainingMultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = 2397239625822397286L;

    private final List<MultifactorAuthenticationProviderBypassEvaluator> multifactorAuthenticationProviderBypassEvaluators
        = new ArrayList<>();

    @Audit(action = "MFA_BYPASS",
        actionResolverName = "MFA_BYPASS_ACTION_RESOLVER",
        resourceResolverName = "MFA_BYPASS_RESOURCE_RESOLVER")
    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {

        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .allMatch(bypass -> bypass.shouldMultifactorAuthenticationProviderExecute(authentication,
                registeredService, provider, request));
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
    public boolean isMultifactorAuthenticationBypassed(final Authentication authentication, final String requestedContext) {
        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .allMatch(bypass -> bypass.isMultifactorAuthenticationBypassed(authentication, requestedContext));
    }

    @Override
    public Optional<MultifactorAuthenticationProviderBypassEvaluator> belongsToMultifactorAuthenticationProvider(final String providerId) {
        return multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .filter(bypass -> bypass.belongsToMultifactorAuthenticationProvider(providerId).isPresent())
            .findFirst();
    }

    @Override
    public String getProviderId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return getProviderId();
    }

    /**
     * Add bypass provider.
     *
     * @param bypass - the bypass provider
     */
    public void addMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassEvaluator bypass) {
        if (!bypass.isEmpty()) {
            this.multifactorAuthenticationProviderBypassEvaluators.add(bypass);
        }
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
    public MultifactorAuthenticationProviderBypassEvaluator filterMultifactorAuthenticationProviderBypassEvaluatorsBy(final String providerId) {
        val chain = new DefaultChainingMultifactorAuthenticationBypassProvider();
        multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .filter(bp -> bp.belongsToMultifactorAuthenticationProvider(providerId).isPresent())
            .forEach(chain::addMultifactorAuthenticationProviderBypassEvaluator);

        if (chain.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return chain;
    }
}
