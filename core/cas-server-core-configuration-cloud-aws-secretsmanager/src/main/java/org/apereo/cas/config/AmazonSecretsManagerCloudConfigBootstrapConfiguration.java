package org.apereo.cas.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

/**
 * This is {@link AmazonSecretsManagerCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("amazonSecretsManagerCloudConfigBootstrapConfiguration")
@Slf4j
@Getter
public class AmazonSecretsManagerCloudConfigBootstrapConfiguration implements PropertySourceLocator {

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final Properties props = new Properties();
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
