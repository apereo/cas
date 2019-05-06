package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.val;

import java.util.List;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderSelector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ChainingMultifactorAuthenticationProviderSelector extends RankedMultifactorAuthenticationProviderSelector {
    @Override
    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(
        final RegisteredService service,
        final List<MultifactorAuthenticationProvider> providers) {

        if (providers.size() > 1) {
            val provider = new DefaultChainingMultifactorAuthenticationProvider();
            provider.addMultifactorAuthenticationProviders(providers);
            return provider;
        }
        return super.selectMultifactorAuthenticationProvider(service, providers);
    }
}
