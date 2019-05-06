package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypass;
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
 * Class used to Chain multiple {@link MultifactorAuthenticationProviderBypass} implementations.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Getter
@NoArgsConstructor
public class DefaultChainingMultifactorAuthenticationBypassProvider implements ChainingMultifactorAuthenticationProviderBypass {
    private static final long serialVersionUID = 2397239625822397286L;

    private final List<MultifactorAuthenticationProviderBypass> multifactorAuthenticationProviderBypassEvaluators
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
            .allMatch(bypass -> bypass.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request));
    }

    @Override
    public Optional<MultifactorAuthenticationProviderBypass> belongsToMultifactorAuthenticationProvider(final String providerId) {
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
    public void addMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypass bypass) {
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
    public MultifactorAuthenticationProviderBypass filterMultifactorAuthenticationProviderBypassBy(final String providerId) {
        val chain = new DefaultChainingMultifactorAuthenticationBypassProvider();
        multifactorAuthenticationProviderBypassEvaluators
            .stream()
            .filter(bp -> bp.belongsToMultifactorAuthenticationProvider(providerId).isPresent())
            .forEach(chain::addMultifactorAuthenticationProviderBypass);

        if (chain.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return chain;
    }
}
