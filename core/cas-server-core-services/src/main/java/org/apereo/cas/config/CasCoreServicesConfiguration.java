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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
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

    private static final String BEAN_NAME_SERVICE_REGISTRY_DAO = "serviceRegistryDao";
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @RefreshScope
    @Bean
    public MultifactorTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {
        final DefaultMultifactorTriggerSelectionStrategy s = new DefaultMultifactorTriggerSelectionStrategy();
        
        s.setGlobalPrincipalAttributeNameTriggers(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers());
        s.setRequestParameter(casProperties.getAuthn().getMfa().getRequestParameter());
        
        return s;
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
        impl.setServiceFactory(this.webApplicationServiceFactory());
        return impl;
    }

    @ConditionalOnMissingBean(name = "serviceRegistryDao")
    @Bean
    public ServiceRegistryDao serviceRegistryDao() {
        final InMemoryServiceRegistryDaoImpl impl = new InMemoryServiceRegistryDaoImpl();
        if (context.containsBean("inMemoryRegisteredServices")) {
            final List list = context.getBean("inMemoryRegisteredServices", List.class);
            impl.setRegisteredServices(list);
        }
        return impl;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer(@Qualifier(BEAN_NAME_SERVICE_REGISTRY_DAO)
                                                                 final ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryInitializer(embeddedJsonServiceRegistry(eventPublisher),
                serviceRegistryDao, servicesManager(serviceRegistryDao),
                casProperties.getServiceRegistry().isInitFromJson());
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryDao embeddedJsonServiceRegistry(final ApplicationEventPublisher publisher) {
        try {
            return new EmbeddedServiceRegistryDao(publisher);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Lazy
    @Bean
    public List serviceFactoryList() {
        final List<ServiceFactory> list = new ArrayList<>();
        list.add(webApplicationServiceFactory());
        return list;
    }
    
    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {
        EmbeddedServiceRegistryDao(final ApplicationEventPublisher publisher) throws Exception {
            super(new ClassPathResource("services"), new RegisteredServiceJsonSerializer(), false, publisher);
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }
}
