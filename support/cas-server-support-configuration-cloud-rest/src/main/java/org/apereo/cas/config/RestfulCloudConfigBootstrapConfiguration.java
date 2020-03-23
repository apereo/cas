package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RestfulCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "restfulCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
public class RestfulCloudConfigBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restfulPropertySourceLocator")
    public PropertySourceLocator restfulPropertySourceLocator() {
        return new RestfulPropertySourceLocator();
    }
}
