package org.jasig.cas.config;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;

/**
 * The {@link ShibbolethAttributeResolverConfiguration}.
 *
 * @author Jj
 * @since 4.3.0
 */
@Configuration("shibbolethAttributeResolverConfiguration")
@ComponentScan("org.jasig.cas.persondir.support")
public class ShibbolethAttributeResolverConfiguration {
    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Value("${shibboleth.attributeResolver.resources}")
    private List<Resource> attributeResolverResources;

    @Autowired(required = false)
    private PlaceholderConfigurerSupport placeholderConfigurerSupport = new PropertyPlaceholderConfigurer();

    @Bean
    @Scope("singleton")
    AttributeResolver attributeResolver() {
        final ApplicationContext tempApplicationContext = SpringSupport.newContext(
                "shibbolethAttributeResolverContext",
                attributeResolverResources,
                Collections.singletonList(placeholderConfigurerSupport),
                Collections.emptyList(),
                Collections.emptyList(),
                applicationContext
        );

        return new AttributeResolverImpl(
                "ShibbolethAttributeResolver",
                BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, AttributeDefinition.class).values(),
                BeanFactoryUtils.beansOfTypeIncludingAncestors(tempApplicationContext, DataConnector.class).values(),
                null
        );
    }
}
