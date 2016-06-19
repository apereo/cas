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
import org.apereo.cas.services.AbstractResourceBasedServiceRegistryDao;
import org.apereo.cas.services.DefaultServicesManagerImpl;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImpl;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreServicesConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext context;

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
    public CasAttributeEncoder casAttributeEncoder(@Qualifier("serviceRegistryDao")
                                                   final ServiceRegistryDao serviceRegistryDao) {
        final DefaultCasAttributeEncoder e =
                new DefaultCasAttributeEncoder(servicesManager(serviceRegistryDao));
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
    public ServicesManager servicesManager(@Qualifier("serviceRegistryDao")
                                           final ServiceRegistryDao serviceRegistryDao) {
        final DefaultServicesManagerImpl impl = new DefaultServicesManagerImpl();
        impl.setServiceRegistryDao(serviceRegistryDao);
        return impl;
    }

    @ConditionalOnMissingBean(name = "serviceRegistryDao")
    @Bean(name = {"serviceRegistryDao", "inMemoryServiceRegistryDao"})
    public ServiceRegistryDao inMemoryServiceRegistryDao() {
        final InMemoryServiceRegistryDaoImpl impl = new InMemoryServiceRegistryDaoImpl();
        if (context.containsBean("inMemoryRegisteredServices")) {
            final List list = context.getBean("inMemoryRegisteredServices", List.class);
            impl.setRegisteredServices(list);
        }
        return impl;
    }

    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer(@Qualifier("serviceRegistryDao")
                                                                 final ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryInitializer(embeddedJsonServiceRegistry(),
                serviceRegistryDao, servicesManager(serviceRegistryDao),
                casProperties.getServiceRegistry().isInitFromJson());
    }

    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryDao embeddedJsonServiceRegistry() {
        try {
            return new EmbeddedServiceRegistryDao();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static class EmbeddedServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {
        EmbeddedServiceRegistryDao() throws Exception {
            super(new ClassPathResource("services"), new RegisteredServiceJsonSerializer(), false);
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }
}
