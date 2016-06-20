package org.apereo.cas.config;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.support.ShibbolethPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Collections;

/**
 * The {@link ShibbolethAttributeResolverConfiguration}.
 *
 * @author Jonathan Johnson
 * @since 5.0.0
 */
@Configuration("shibbolethAttributeResolverConfiguration")
@ComponentScan("org.apereo.cas.persondir.support")
public class ShibbolethAttributeResolverConfiguration {
    
    @Autowired(required = false)
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private PlaceholderConfigurerSupport placeholderConfigurerSupport = new PropertyPlaceholderConfigurer();

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @RefreshScope
    @Bean
    @Scope("singleton")
    AttributeResolver attributeResolver() {
        final ApplicationContext tempApplicationContext = SpringSupport.newContext(
                "shibbolethAttributeResolverContext",
                casProperties.getShibAttributeResolver().getResources(),
                Collections.singletonList(this.placeholderConfigurerSupport),
                Collections.emptyList(),
                Collections.emptyList(),
                this.applicationContext
        );

        return new AttributeResolverImpl(
                "ShibbolethAttributeResolver",
                BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, AttributeDefinition.class).values(),
                BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, DataConnector.class).values(),
                null
        );
    }
    
    @Bean(name={"shibbolethPersonAttributeDao", "attributeRepository"})
    public IPersonAttributeDao shibbolethPersonAttributeDao() {
        final ShibbolethPersonAttributeDao d = new ShibbolethPersonAttributeDao();
        d.setAttributeResolver(attributeResolver());
        return d;
    }
}
