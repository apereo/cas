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
import org.apereo.cas.services.DefaultServicesManagerImpl;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImpl;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ReloadableServicesManager;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
public class CasCoreServicesConfiguration {

    @Value("${service.registry.config.location:classpath:services}")
    private Resource configDirectory;
    
    @Value("${service.registry.watcher.enabled:true}")
    private boolean enableWatcher;
            
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
    @Autowired
    @Bean
    public CasAttributeEncoder casAttributeEncoder(@Qualifier("servicesManager") 
                                                   final ServicesManager servicesManager) {
        return new DefaultCasAttributeEncoder(servicesManager);
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
        return new DefaultServicesManagerImpl();
    }

    @Bean
    public ServiceRegistryDao inMemoryServiceRegistryDao() {
        return new InMemoryServiceRegistryDaoImpl();
    }
    
    @Bean
    public ServiceRegistryDao jsonServiceRegistryDao() {
        try {
            return new JsonServiceRegistryDao(this.configDirectory, this.enableWatcher);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }
    
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer() {
        return new ServiceRegistryInitializer();
    }
}
