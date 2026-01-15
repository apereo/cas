package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.model.core.config.cloud.SpringCloudConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * This is {@link RestfulPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestfulPropertySourceLocator implements PropertySourceLocator {
    @Override
    public PropertySource<?> locate(final Environment environment) {
        val config = Binder.get(environment).bind(RestfulPropertySource.CAS_CONFIGURATION_PREFIX, SpringCloudConfigurationProperties.Rest.class)
            .orElseThrow(() -> new IllegalArgumentException("No RESTful configuration settings are defined"));
        return new RestfulPropertySource(RestfulPropertySource.class.getName(), config);
    }
}
