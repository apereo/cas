package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultChainingMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class DefaultChainingMultifactorAuthenticationProvider implements ChainingMultifactorAuthenticationProvider {
    private static final long serialVersionUID = -3199297701531604341L;

    private final List<MultifactorAuthenticationProvider> multifactorAuthenticationProviders = new ArrayList<>(0);

    private final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    @Override
    public MultifactorAuthenticationProviderBypassEvaluator getBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        getMultifactorAuthenticationProviders()
            .stream()
            .sorted(OrderComparator.INSTANCE)
            .map(MultifactorAuthenticationProvider::getBypassEvaluator)
            .forEach(bypass::addMultifactorAuthenticationProviderBypassEvaluator);
        return bypass;
    }

    @Override
    public MultifactorAuthenticationProvider addMultifactorAuthenticationProvider(
        final MultifactorAuthenticationProvider provider) {
        multifactorAuthenticationProviders.add(provider);
        return provider;
    }

    @Override
    public void addMultifactorAuthenticationProviders(final Collection<MultifactorAuthenticationProvider> providers) {
        multifactorAuthenticationProviders.addAll(providers);
    }
}
