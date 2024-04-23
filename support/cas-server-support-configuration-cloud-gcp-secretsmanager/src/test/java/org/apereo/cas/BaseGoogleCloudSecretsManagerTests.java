package org.apereo.cas;

import org.apereo.cas.config.GoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * This is {@link BaseGoogleCloudSecretsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    BaseGoogleCloudSecretsManagerTests.BaseGoogleCloudSecretsManagerTestConfiguration.class,
    PropertyPlaceholderAutoConfiguration.class,
    GoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration.class
}, properties = {
    "spring.cloud.gcp.secretmanager.enabled=true",
    "spring.cloud.gcp.secretmanager.project-id=project-12345"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseGoogleCloudSecretsManagerTests {
    @Autowired
    @Qualifier("googleCloudSecretsManagerPropertySourceLocator")
    protected PropertySourceLocator propertySourceLocator;

    @Autowired
    protected Environment environment;

    @TestConfiguration(value = "BaseGoogleCloudSecretsManagerTestConfiguration", proxyBeanMethods = false)
    static class BaseGoogleCloudSecretsManagerTestConfiguration {
        @Bean
        public CredentialsProvider googleCloudSecretsManagerCredentialProvider() {
            return new NoCredentialsProvider();
        }
    }
}
