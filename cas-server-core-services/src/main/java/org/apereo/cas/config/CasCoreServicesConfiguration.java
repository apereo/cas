package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.authentication.support.CasAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpCasAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultServicesManagerImpl;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImpl;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ReloadableServicesManager;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
public class CasCoreServicesConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Autowired(required = false)
    @Qualifier("inMemoryRegisteredServices")
    private List inMemoryRegisteredServices;

    @Autowired
    @Qualifier("servicesManager")
    private ReloadableServicesManager servicesManager;
    
    @RefreshScope
    @Bean
    public MultifactorTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {
        return new DefaultMultifactorTriggerSelectionStrategy();
    }

    @RefreshScope
    @Bean
    public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
        return new ShibbolethCompatiblePersistentIdGenerator();
    }

    @Bean
    public ServiceFactory webApplicationServiceFactory() {
        return new WebApplicationServiceFactory();
    }

    @RefreshScope
    @Bean
    public CasAttributeEncoder casAttributeEncoder() {
        final DefaultCasAttributeEncoder e = new DefaultCasAttributeEncoder(servicesManager());
        e.setCipherExecutor(registeredServiceCipherExecutor());
        return e;
    }

    @Bean
    public NoOpCasAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpCasAttributeEncoder();
    }

    @Bean
    public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
        return new DefaultRegisteredServiceCipherExecutor();
    }


    @Bean
    public ReloadableServicesManager servicesManager() {
        return new DefaultServicesManagerImpl(serviceRegistryDao);
    }

    @Bean
    public ServiceRegistryDao inMemoryServiceRegistryDao() {
        final InMemoryServiceRegistryDaoImpl impl = new InMemoryServiceRegistryDaoImpl();
        impl.setRegisteredServices(inMemoryRegisteredServices);
        return impl;
    }

    @Bean
    public ServiceRegistryDao jsonServiceRegistryDao() {
        try {
            final JsonServiceRegistryDao dao =
                    new JsonServiceRegistryDao(casProperties.getServiceRegistry().getConfig().getLocation(),
                    casProperties.getServiceRegistry().isWatcherEnabled());
            dao.setServicesManager(this.servicesManager);
            return dao;
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer() {
        return new ServiceRegistryInitializer(jsonServiceRegistryDao(),
                serviceRegistryDao, servicesManager(),
                casProperties.getServiceRegistry().isInitFromJson());
    }
}
