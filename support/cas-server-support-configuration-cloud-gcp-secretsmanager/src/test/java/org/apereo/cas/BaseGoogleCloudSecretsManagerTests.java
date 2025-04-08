package org.apereo.cas;

import org.apereo.cas.config.CasGoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * This is {@link BaseGoogleCloudSecretsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseGoogleCloudSecretsManagerTests.BaseGoogleCloudSecretsManagerTestConfiguration.class,
    CasGoogleCloudSecretsManagerCloudConfigBootstrapAutoConfiguration.class
}, properties = {
    "spring.cloud.gcp.secretmanager.enabled=true",
    "spring.cloud.gcp.secretmanager.project-id=project-12345"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
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
