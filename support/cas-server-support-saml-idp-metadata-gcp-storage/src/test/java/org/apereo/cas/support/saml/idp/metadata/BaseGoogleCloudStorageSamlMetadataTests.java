package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.config.CasSamlIdPGoogleCloudStorageAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
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
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link BaseGoogleCloudStorageSamlMetadataTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SpringBootTest(classes = {
    BaseGoogleCloudStorageSamlMetadataTests.GoogleCloudStorageTestConfiguration.class,
    CasSamlIdPGoogleCloudStorageAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class,
    GcpStorageAutoConfiguration.class,
    GcpContextAutoConfiguration.class
}, properties = {
    "spring.cloud.gcp.storage.enabled=true",
    "spring.cloud.gcp.storage.project-id=apereo-cas-gcp",
    "spring.cloud.gcp.storage.host=http://127.0.0.1:8100"
})
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("GCP")
abstract class BaseGoogleCloudStorageSamlMetadataTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

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
