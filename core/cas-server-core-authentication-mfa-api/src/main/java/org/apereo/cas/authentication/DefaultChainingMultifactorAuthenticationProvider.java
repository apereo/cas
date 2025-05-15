package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Serial
    private static final long serialVersionUID = -3199297701531604341L;

    private final ConfigurableApplicationContext applicationContext;

    private final List<MultifactorAuthenticationProvider> multifactorAuthenticationProviders = new ArrayList<>();

    private final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    @Override
    public MultifactorAuthenticationProviderBypassEvaluator getBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        getMultifactorAuthenticationProviders()
            .stream()
            .filter(BeanSupplier::isNotProxy)
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

    @Override
    public MultifactorAuthenticationDeviceManager getDeviceManager() {
        return new MultifactorAuthenticationDeviceManager() {
            @Override
            public List<String> getSource() {
                return getMultifactorAuthenticationProviders()
                    .stream()
                    .filter(BeanSupplier::isNotProxy)
                    .sorted(OrderComparator.INSTANCE)
                    .map(MultifactorAuthenticationProvider::getDeviceManager)
                    .filter(Objects::nonNull)
                    .map(MultifactorAuthenticationDeviceManager::getSource)
                    .flatMap(List::stream)
                    .toList();
            }

            @Override
            public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
                return getMultifactorAuthenticationProviders()
                    .stream()
                    .filter(BeanSupplier::isNotProxy)
                    .sorted(OrderComparator.INSTANCE)
                    .map(MultifactorAuthenticationProvider::getDeviceManager)
                    .filter(Objects::nonNull)
                    .map(deviceManager -> deviceManager.findRegisteredDevices(principal))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            }
        };
    }
}
