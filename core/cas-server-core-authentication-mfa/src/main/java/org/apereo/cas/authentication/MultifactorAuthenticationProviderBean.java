package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Generic class that applies a list of {@link BaseMultifactorProviderProperties} to a provided
 * {@link MultifactorAuthenticationProviderFactory} to create instances of {@link MultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 * @param <T> - Type of {@link MultifactorAuthenticationProvider}
 * @param <P> - Type of {@link BaseMultifactorProviderProperties}
 */
@RequiredArgsConstructor
@Slf4j
public class MultifactorAuthenticationProviderBean<T extends MultifactorAuthenticationProvider,
                                                   P extends BaseMultifactorProviderProperties> {

    private final MultifactorAuthenticationProviderFactory<T, P> providerFactory;

    private final DefaultListableBeanFactory beanFactory;

    private final List<P> properties;

    /**
     * Method destroys and then recreates all singletons of the providers.
     */
    @PostConstruct
    protected void create() {
        properties.stream().forEach(p -> {
            final String name = providerFactory.beanName(p.getId());
            beanFactory.destroySingleton(name);
            beanFactory.registerSingleton(name, providerFactory.create(p));
        });
    }

    /**
     * Returns the provider assigned to the passed id.
     *
     * @param id - the id
     * @return {@link MultifactorAuthenticationProvider}
     */
    public T getProvider(final String id) {
        return (T) beanFactory.getBean(providerFactory.beanName(id), MultifactorAuthenticationProvider.class);
    }

    /**
     * Provider instances are not part of the {@link org.springframework.cloud.context.scope.refresh.RefreshScope}
     * since they were not defined during initial autoconfiguration. The {@link MultifactorAuthenticationProviderBean} is
     * usually not consulted after initial configuration.  This listener is here so this bean is initialized after a
     * refresh and the provider instances get recreated.
     *
     * @param event - the event.
     */
    @EventListener
    public void onRefreshScopeRefreshed(final RefreshScopeRefreshedEvent event) {
        LOGGER.debug("Refreshing MFA Providers");
    }


}
