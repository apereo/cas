package org.apereo.cas.config;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.persondir.support.ShibbolethPersonAttributeDao;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The {@link ShibbolethAttributeResolverConfiguration}.
 *
 * @author Jonathan Johnson
 * @since 5.0.0
 */
@Configuration("shibbolethAttributeResolverConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class ShibbolethAttributeResolverConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethAttributeResolverConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public IPersonAttributeDao attributeRepository() {
        try {
            final PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
            final Map<String, Object> result = new HashMap<>();
            environment.getPropertySources().forEach(s -> {
                if (s instanceof EnumerablePropertySource<?>) {
                    final EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) s;
                    final List<String> names = CollectionUtils.wrapList(ps.getPropertyNames());
                    names.forEach(key -> result.put(key, ps.getProperty(key)));
                }
            });
            final Properties p = new Properties();
            p.putAll(result);
            cfg.setProperties(p);

            registerBeanIntoApplicationContext(cfg, "shibboleth.PropertySourcesPlaceholderConfigurer");
            final ApplicationContext tempApplicationContext = SpringSupport.newContext(
                    getClass().getName(),
                    casProperties.getShibAttributeResolver().getResources(),
                    CollectionUtils.wrap(cfg),
                    new ArrayList<>(0),
                    new ArrayList<>(0),
                    this.applicationContext
            );

            final Collection<DataConnector> values = BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, DataConnector.class).values();
            final Collection<DataConnector> connectors = new HashSet<>(values);
            final AttributeResolverImpl impl = new AttributeResolverImpl();
            impl.setId(getClass().getSimpleName());
            impl.setApplicationContext(tempApplicationContext);
            impl.setAttributeDefinitions(BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, AttributeDefinition.class).values());
            impl.setDataConnectors(connectors);
            if (!impl.isInitialized()) {
                impl.initialize();
            }
            return new ShibbolethPersonAttributeDao(impl);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
    }

    /**
     * Register bean into application context.
     *
     * @param beanInstance the bean instance
     * @param name         the name
     */
    protected void registerBeanIntoApplicationContext(final Object beanInstance, final String name) {
        final Object initBean = this.applicationContext.getAutowireCapableBeanFactory().initializeBean(beanInstance, name);
        final ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        factory.registerSingleton(name, initBean);
    }
}
