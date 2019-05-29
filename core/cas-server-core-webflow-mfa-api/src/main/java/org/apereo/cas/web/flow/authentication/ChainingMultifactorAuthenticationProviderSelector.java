package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderSelector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class ChainingMultifactorAuthenticationProviderSelector extends RankedMultifactorAuthenticationProviderSelector {

    private final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    @Override
    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(
        final RegisteredService service,
        final List<MultifactorAuthenticationProvider> providers) {

        if (providers.size() > 1) {
            val provider = new DefaultChainingMultifactorAuthenticationProvider(failureModeEvaluator);
            provider.addMultifactorAuthenticationProviders(providers);
            return provider;
        }
        return super.selectMultifactorAuthenticationProvider(service, providers);
    }
}
