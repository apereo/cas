package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * This is {@link AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "amazonSecretsManagerCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
@Slf4j
@Getter
public class AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    /**
     * Configuration prefix for amazon secrets manager.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.aws.ssm";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();
        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val client = builder.build(AWSSimpleSystemsManagementClientBuilder.standard(), AWSSimpleSystemsManagement.class);

            val profiles = new ArrayList<String>();
            profiles.add(StringUtils.EMPTY);
            profiles.addAll(Arrays.asList(environment.getActiveProfiles()));

            profiles.forEach(profile -> {
                var nextToken = (String) null;
                do {
                    val prefix = String.format("/cas/%s", profile);
                    val request = new GetParametersByPathRequest()
                        .withPath(prefix)
                        .withWithDecryption(Boolean.TRUE)
                        .withNextToken(nextToken);
                    val result = client.getParametersByPath(request);
                    nextToken = result.getNextToken();
                    LOGGER.trace("Fetched [{}] parameters with next token as [{}}", result.getParameters().size(), result.getNextToken());

                    result.getParameters().forEach(p -> {
                        val key = StringUtils.removeEnd(StringUtils.removeStart(p.getName(), prefix), "/");
                        props.put(key, p.getValue());
                    });
                } while (nextToken != null);
            });
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.debug("Located [{}] settings(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
