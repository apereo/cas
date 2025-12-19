package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasGoogleCloudFirestoreServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.ApiKeyCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.firestore.GcpFirestoreAutoConfiguration;
import com.google.cloud.spring.autoconfigure.firestore.GcpFirestoreProperties;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.firestore.v1.FirestoreGrpc;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudFirestoreServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Tag("GCP")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleCloudFirestoreServiceRegistryTests.GoogleCloudFirestoreTestConfiguration.class,
    CasGoogleCloudFirestoreServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    GcpFirestoreAutoConfiguration.class,
    GcpContextAutoConfiguration.class
}, properties = {
    "spring.cloud.gcp.firestore.project-id=apereo-cas-gcp",

    "spring.cloud.gcp.firestore.emulator.enabled=true",
    "spring.cloud.gcp.firestore.host-port=127.0.0.1:9980"
})
class GoogleCloudFirestoreServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;

    @TestConfiguration(value = "GoogleCloudFirestoreTestConfiguration", proxyBeanMethods = false)
    static class GoogleCloudFirestoreTestConfiguration {

        @Bean
        public GcpProjectIdProvider gcpProjectIdProvider(final GcpFirestoreProperties properties) {
            return properties::getProjectId;
        }

        @Bean
        public FirestoreGrpc.FirestoreStub firestoreGrpcStub() {
            return mock(FirestoreGrpc.FirestoreStub.class);
        }

        @Bean
        public FirestoreOptions firestoreOptions(final GcpFirestoreProperties properties) {
            return FirestoreOptions.getDefaultInstance().toBuilder()
                .setCredentials(ApiKeyCredentials.create(UUID.randomUUID().toString()))
                .setProjectId(properties.getProjectId())
                .setChannelProvider(InstantiatingGrpcChannelProvider.newBuilder()
                    .setEndpoint(properties.getHostPort())
                    .build())
                .setEmulatorHost(properties.getHostPort())
                .setDatabaseId(UUID.randomUUID().toString())
                .build();
        }
    }
}
