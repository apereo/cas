package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;

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
            val client = builder.build(SsmClient.builder(), SsmClient.class);

            val profiles = new ArrayList<String>();
            profiles.add(StringUtils.EMPTY);
            profiles.addAll(Arrays.asList(environment.getActiveProfiles()));

            profiles.forEach(profile -> {
                var nextToken = (String) null;
                do {
                    val prefix = String.format("/cas/%s", profile);
                    val request = GetParametersByPathRequest.builder()
                        .path(prefix)
                        .withDecryption(Boolean.TRUE)
                        .nextToken(nextToken)
                        .build();
                    val result = client.getParametersByPath(request);
                    nextToken = result.nextToken();
                    LOGGER.trace("Fetched [{}] parameters with next token as [{}]", result.parameters().size(), result.nextToken());

                    result.parameters().forEach(p -> {
                        val key = StringUtils.removeEnd(StringUtils.removeStart(p.name(), prefix), "/");
                        props.put(key, p.value());
                    });
                } while (nextToken != null);
            });
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.debug("Located [{}] settings(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }
}
