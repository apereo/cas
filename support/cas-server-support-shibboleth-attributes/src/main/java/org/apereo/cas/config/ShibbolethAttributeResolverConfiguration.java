package org.apereo.cas.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.support.ShibbolethPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Collections;
import java.util.HashMap;
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
public class ShibbolethAttributeResolverConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = {"shibbolethPersonAttributeDao", "attributeRepository"})
    public IPersonAttributeDao shibbolethPersonAttributeDao() {
        try {
            final PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
            final Map<String, Object> result = new HashMap<>();
            environment.getPropertySources().forEach(s -> {
                if (s instanceof EnumerablePropertySource<?>) {
                    final EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) s;
                    Lists.newArrayList(ps.getPropertyNames()).forEach(key -> result.put(key, ps.getProperty(key)));
                }
            });
            final Properties p = new Properties();
            p.putAll(result);
            cfg.setProperties(p);
            final ApplicationContext tempApplicationContext = SpringSupport.newContext(
                    getClass().getName(),
                    casProperties.getShibAttributeResolver().getResources(),
                    Collections.singletonList(cfg),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    this.applicationContext
            );

            final AttributeResolverImpl impl = new AttributeResolverImpl(
                    getClass().getSimpleName(),
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, AttributeDefinition.class).values(),
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, DataConnector.class).values(),
                    null
            );

            if (!impl.isInitialized()) {
                impl.initialize();
            }
            return new ShibbolethPersonAttributeDao(impl);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
