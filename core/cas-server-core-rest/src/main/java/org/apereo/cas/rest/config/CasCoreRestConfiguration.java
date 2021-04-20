package org.apereo.cas.rest.config;

import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * This is {@link CasCoreRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "CasCoreRestConfiguration", proxyBeanMethods = false)
@Slf4j
public class CasCoreRestConfiguration {
    @Autowired
    @Bean
    public RestHttpRequestCredentialFactory restHttpRequestCredentialFactory(final List<RestHttpRequestCredentialFactoryConfigurer> configurers) {
        LOGGER.trace("building REST credential factory from [{}]", configurers);

        val factory = new ChainingRestHttpRequestCredentialFactory();
        AnnotationAwareOrderComparator.sortIfNecessary(configurers);

        configurers.forEach(c -> {
            LOGGER.trace("Configuring credential factory: [{}]", c);
            c.configureCredentialFactory(factory);
        });
        return factory;
    }

    @ConditionalOnMissingBean(name = "restHttpRequestCredentialFactoryConfigurer")
    @Bean
    public RestHttpRequestCredentialFactoryConfigurer restHttpRequestCredentialFactoryConfigurer() {
        return factory -> factory.registerCredentialFactory(new UsernamePasswordRestHttpRequestCredentialFactory());
    }
}
