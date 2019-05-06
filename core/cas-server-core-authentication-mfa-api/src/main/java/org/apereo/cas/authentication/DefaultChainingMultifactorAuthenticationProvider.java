package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultChainingMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
public class DefaultChainingMultifactorAuthenticationProvider implements ChainingMultifactorAuthenticationProvider {
    private static final long serialVersionUID = -3199297701531604341L;

    private final List<MultifactorAuthenticationProvider> multifactorAuthenticationProviders = new ArrayList<>();

    @Override
    public MultifactorAuthenticationProviderBypass getBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        getMultifactorAuthenticationProviders()
            .stream()
            .sorted()
            .map(MultifactorAuthenticationProvider::getBypassEvaluator)
            .forEach(bypass::addMultifactorAuthenticationProviderBypass);
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
