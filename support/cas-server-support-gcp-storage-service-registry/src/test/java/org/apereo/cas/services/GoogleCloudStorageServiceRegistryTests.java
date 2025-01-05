package org.apereo.cas.services;

import org.apereo.cas.config.CasGoogleCloudStorageServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.ApiKeyCredentials;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageAutoConfiguration;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageProperties;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.core.UserAgentHeaderProvider;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.io.IOException;
import java.util.UUID;

/**
 * This is {@link GoogleCloudStorageServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Tag("GCP")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleCloudStorageServiceRegistryTests.GoogleCloudStorageTestConfiguration.class,
    CasGoogleCloudStorageServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    GcpStorageAutoConfiguration.class,
    GcpContextAutoConfiguration.class
}, properties = {
    "spring.cloud.gcp.storage.enabled=true",
    "spring.cloud.gcp.storage.project-id=apereo-cas-gcp",
    "spring.cloud.gcp.storage.host=http://127.0.0.1:8100"
})
class GoogleCloudStorageServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;

    @TestConfiguration(value = "GoogleCloudStorageTestConfiguration", proxyBeanMethods = false)
    static class GoogleCloudStorageTestConfiguration {
        @Bean
        public GcpProjectIdProvider gcpProjectIdProvider(final GcpStorageProperties properties) {
            return properties::getProjectId;
        }

        @Bean
        public CredentialsProvider googleCredentialsProvider() {
            return () -> ApiKeyCredentials.create(UUID.randomUUID().toString());
        }

        @Bean
        public Storage storage(final GcpProjectIdProvider gcpProjectIdProvider,
                               final CredentialsProvider googleCredentialsProvider,
                               final GcpStorageProperties properties) throws IOException {
            val storageOptionsBuilder = StorageOptions.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(GcpStorageAutoConfiguration.class))
                .setProjectId(gcpProjectIdProvider.getProjectId())
                .setCredentials(googleCredentialsProvider.getCredentials());
            storageOptionsBuilder.setHost(properties.getHost());
            return storageOptionsBuilder.build().getService();
        }

    }
}
