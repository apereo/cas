package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * Generic class that applies a list of {@link BaseMultifactorProviderProperties} to a provided
 * {@link MultifactorAuthenticationProviderFactoryBean} to create instances of {@link MultifactorAuthenticationProvider}.
 *
 * @param <T> - Type of {@link MultifactorAuthenticationProvider}
 * @param <P> - Type of {@link BaseMultifactorProviderProperties}
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
@Slf4j
public class MultifactorAuthenticationProviderBean<T extends MultifactorAuthenticationProvider,
    P extends BaseMultifactorProviderProperties> implements InitializingBean {

    private final MultifactorAuthenticationProviderFactoryBean<T, P> providerFactory;

    private final DefaultListableBeanFactory beanFactory;

    private final List<P> properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        properties.forEach(p -> {
            val name = providerFactory.beanName(p.getId());
            beanFactory.destroySingleton(name);
            beanFactory.registerSingleton(name, providerFactory.create(p));
        });
    }

    /**
     * Returns the provider assigned to the passed id.
     *
     * @param <T>   the type parameter
     * @param id    - the id
     * @param clazz the clazz
     * @return {@link MultifactorAuthenticationProvider}
     */
    public <T extends MultifactorAuthenticationProvider> T getProvider(final String id, final Class<T> clazz) {
        return beanFactory.getBean(providerFactory.beanName(id), clazz);
    }

    /**
     * Provider instances are not part of the {@link org.springframework.cloud.context.scope.refresh.RefreshScope}
     * since they were not defined during initial auto-configuration. This class is
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
