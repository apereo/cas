package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.spring.autoconfigure.secretmanager.GcpSecretManagerAutoConfiguration;
import com.google.cloud.spring.autoconfigure.secretmanager.GcpSecretManagerProperties;
import com.google.cloud.spring.core.DefaultCredentialsProvider;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.core.UserAgentHeaderProvider;
import com.google.cloud.spring.secretmanager.SecretManagerPropertySource;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * This is {@link CasGoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "gcp-secretsmanager")
@ConditionalOnProperty(value = "spring.cloud.gcp.secretmanager.enabled", matchIfMissing = true)
@AutoConfiguration(after = GcpSecretManagerAutoConfiguration.class)
@EnableConfigurationProperties(GcpSecretManagerProperties.class)
public class CasGoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration {

    /**
     * Google cloud secrets manager property source.
     * The implementation here overrides the property fetching mechanism to
     * convert the final secret into a String. Otherwise, the actual
     * value of the secret is passed back to the environment
     * as {@code ByteString@abcdefg size=19 contents=...}.
     * This could possibly be a bug in Spring Cloud GCP where
     * {@link com.google.cloud.spring.autoconfigure.secretmanager.GcpSecretManagerEnvironmentPostProcessor}
     * is not doing its job correctly.
     * @param properties the properties
     * @return the property source locator
     */
    @Bean
    @ConditionalOnMissingBean(name = "googleCloudSecretsManagerPropertySourceLocator")
    public PropertySourceLocator googleCloudSecretsManagerPropertySourceLocator(
        @Qualifier("googleCloudSecretsManagerTemplate")
        final SecretManagerTemplate googleCloudSecretsManagerTemplate,
        final GcpSecretManagerProperties properties) {
        return environment -> Unchecked.supplier(() -> {
            val projectIdProvider = getProjectIdProvider(properties);
            return new SecretManagerPropertySource(getClass().getSimpleName(), googleCloudSecretsManagerTemplate, projectIdProvider) {
                @Override
                public Object getProperty(final String name) {
                    var propertyValue = FunctionUtils.doAndHandle(() -> super.getProperty(name), e -> null).get();
                    return Optional.ofNullable(propertyValue)
                        .map(ByteString.class::cast)
                        .map(ByteString::toStringUtf8)
                        .orElse(null);
                }
            };
        }).get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudSecretsManagerTemplate")
    public SecretManagerTemplate googleCloudSecretsManagerTemplate(
        @Qualifier("googleCloudSecretsManagerCredentialProvider")
        final CredentialsProvider googleCloudSecretsManagerCredentialProvider,
        final GcpSecretManagerProperties properties) throws Exception {
        val settings = SecretManagerServiceSettings.newBuilder()
            .setCredentialsProvider(googleCloudSecretsManagerCredentialProvider)
            .setHeaderProvider(new UserAgentHeaderProvider(getClass()))
            .build();
        val serviceClient = SecretManagerServiceClient.create(settings);
        val projectIdProvider = getProjectIdProvider(properties);
        val secretManagerTemplate = new SecretManagerTemplate(serviceClient, projectIdProvider);
        secretManagerTemplate.setAllowDefaultSecretValue(properties.isAllowDefaultSecret());
        return secretManagerTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudSecretsManagerCredentialProvider")
    public CredentialsProvider googleCloudSecretsManagerCredentialProvider(final GcpSecretManagerProperties properties) throws Exception {
        return new DefaultCredentialsProvider(properties);
    }

    private static GcpProjectIdProvider getProjectIdProvider(final GcpSecretManagerProperties properties) {
        return StringUtils.isNotBlank(properties.getProjectId())
            ? properties::getProjectId
            : new DefaultGcpProjectIdProvider();
    }
}
