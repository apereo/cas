package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasGoogleCloudStorageServiceRegistryAutoConfiguration;
import org.apereo.cas.services.CasGoogleCloudServiceRegistryMessageReceiver.EventTypes;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.ApiKeyCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageAutoConfiguration;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageProperties;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.core.UserAgentHeaderProvider;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasGoogleCloudStorageServiceRegistryListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Tag("GCP")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasGoogleCloudStorageServiceRegistryListenerTests.ServiceRegistryListenerTestConfiguration.class,
    CasGoogleCloudStorageServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    GcpStorageAutoConfiguration.class,
    GcpContextAutoConfiguration.class
}, properties = {
    "CasFeatureModule.ServiceRegistry.gcp-storage-pubsub.enabled=true",
    "spring.cloud.gcp.storage.enabled=true",
    "spring.cloud.gcp.storage.project-id=apereo-cas-gcp",
    "spring.cloud.gcp.storage.host=http://127.0.0.1:8100"
})
@EnabledIfListeningOnPort(port = {8100, 8085})
class CasGoogleCloudStorageServiceRegistryListenerTests {
    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;

    @Autowired
    private GcpStorageProperties gcpStorageProperties;

    @Autowired
    @Qualifier("googleCloudStorageServiceRegistryListener")
    private CasGoogleCloudStorageServiceRegistryListener googleCloudStorageServiceRegistryListener;

    @Autowired
    @Qualifier("googleCloudTransportChannelProvider")
    private TransportChannelProvider googleCloudTransportChannelProvider;

    @Autowired
    @Qualifier(RegisteredServiceResourceNamingStrategy.BEAN_NAME)
    private RegisteredServiceResourceNamingStrategy namingStrategy;

    @Test
    void verifyOperation() throws Exception {
        getNewServiceRegistry().deleteAll();
        val topicName = TopicName.of(gcpStorageProperties.getProjectId(),
            CasGoogleCloudStorageServiceRegistryListener.SUBSCRIPTION_NAME);
        val publisher = Publisher.newBuilder(topicName)
            .setChannelProvider(googleCloudTransportChannelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build();

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val savedService = getNewServiceRegistry().save(registeredService);
        getNewServiceRegistry().load();


        val bucket = GoogleCloudStorageServiceRegistry.determineBucketForRegisteredService(savedService);
        val name = namingStrategy.build(savedService, "json");
        publishEventForType(bucket, name, publisher, EventTypes.OBJECT_FINALIZE);
        publishEventForType(bucket, name, publisher, EventTypes.OBJECT_DELETE);
    }

    private static void publishEventForType(final String bucket,
                                            final String name,
                                            final Publisher publisher,
                                            final EventTypes type) throws Exception {
        val message = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8("""
                {
                  "bucket": "%s",
                  "name": "%s",
                  "generation": "1"
                }
                """.formatted(bucket, name)))
            .putAttributes("eventType", type.name())
            .putAttributes("bucketId", bucket)
            .putAttributes("objectId", name)
            .putAttributes("objectGeneration", "1")
            .putAttributes("payloadFormat", "JSON_API_V1")
            .build();
        publisher.publish(message).get();
    }


    @TestConfiguration(value = "GoogleCloudStoragServiceRegistryListenerTestConfiguration", proxyBeanMethods = false)
    static class ServiceRegistryListenerTestConfiguration {
        @Bean
        public GcpProjectIdProvider gcpProjectIdProvider(final GcpStorageProperties properties) {
            return properties::getProjectId;
        }

        @Bean
        public Storage storage(final GcpProjectIdProvider gcpProjectIdProvider,
                               @Qualifier("googleCredentialsProvider") final CredentialsProvider googleCredentialsProvider,
                               final GcpStorageProperties properties) throws IOException {
            val storageOptionsBuilder = StorageOptions.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(GcpStorageAutoConfiguration.class))
                .setProjectId(gcpProjectIdProvider.getProjectId())
                .setCredentials(googleCredentialsProvider.getCredentials());
            storageOptionsBuilder.setHost(properties.getHost());
            return storageOptionsBuilder.build().getService();
        }

        @Bean
        public TransportChannelProvider googleCloudTransportChannelProvider() {
            val pubsubEmulatorManagedChannel = ManagedChannelBuilder
                .forTarget("127.0.0.1:8085")
                .usePlaintext()
                .build();
            return FixedTransportChannelProvider.create(GrpcTransportChannel.create(pubsubEmulatorManagedChannel));
        }

        @Bean
        public CasGoogleCloudStorageSubscriptionCustomizer googleCloudStorageSubscriptionCustomizer(
            @Qualifier("googleCloudTransportChannelProvider") final TransportChannelProvider googleCloudTransportChannelProvider) {
            return builder -> {
                builder.setChannelProvider(googleCloudTransportChannelProvider);
                builder.setCredentialsProvider(NoCredentialsProvider.create());
                return builder;
            };
        }

        @Bean
        public CredentialsProvider googleCredentialsProvider() {
            return () -> ApiKeyCredentials.create(UUID.randomUUID().toString());
        }

        @Bean
        public TopicAdminSettings topicAdminSettings(
            @Qualifier("googleCloudTransportChannelProvider") final TransportChannelProvider googleCloudTransportChannelProvider) throws Exception {
            return TopicAdminSettings.newBuilder()
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setTransportChannelProvider(googleCloudTransportChannelProvider)
                .build();
        }

        @Bean
        public SubscriptionAdminSettings subscriptionAdminSettings(
            @Qualifier("googleCloudTransportChannelProvider") final TransportChannelProvider googleCloudTransportChannelProvider) throws Exception {
            return SubscriptionAdminSettings.newBuilder()
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setTransportChannelProvider(googleCloudTransportChannelProvider)
                .build();
        }

        @Bean
        public InitializingBean googleCloudStorageServiceRegistryTopics(
            final GcpStorageProperties gcpStorageProperties,
            @Qualifier("subscriptionAdminSettings") final SubscriptionAdminSettings subscriptionAdminSettings,
            @Qualifier("topicAdminSettings") final TopicAdminSettings topicAdminSettings) {
            return () -> {
                val topicName = TopicName.of(gcpStorageProperties.getProjectId(),
                    CasGoogleCloudStorageServiceRegistryListener.SUBSCRIPTION_NAME);

                val subscription = SubscriptionName.of(gcpStorageProperties.getProjectId(),
                    CasGoogleCloudStorageServiceRegistryListener.SUBSCRIPTION_NAME);

                try (val topicAdmin = TopicAdminClient.create(topicAdminSettings)) {
                    FunctionUtils.doAndHandle(_ -> topicAdmin.createTopic(topicName));
                }

                try (val subscriptionAdmin = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
                    FunctionUtils.doAndHandle(_ ->
                        subscriptionAdmin.createSubscription(
                            subscription,
                            topicName,
                            PushConfig.getDefaultInstance(),
                            10
                        ));
                }
            };
        }
    }
}
