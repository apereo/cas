package org.apereo.cas.services;

import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link GoogleCloudStorageServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class GoogleCloudStorageServiceRegistry extends AbstractServiceRegistry {
    private static final String BUCKET_PREFIX = "cas-";
    private static final String FILE_EXTENSION = "json";

    private final StringSerializer<RegisteredService> serializer;
    private final Storage cloudStorage;
    private final RegisteredServiceResourceNamingStrategy namingStrategy;

    public GoogleCloudStorageServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                             final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                             final RegisteredServiceResourceNamingStrategy namingStrategy,
                                             final Storage cloudStorage) {
        super(applicationContext, serviceRegistryListeners);
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
        this.cloudStorage = cloudStorage;
        this.namingStrategy = namingStrategy;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return FunctionUtils.doUnchecked(() -> {
            registeredService.assignIdIfNecessary();
            invokeServiceRegistryListenerPreSave(registeredService);
            LOGGER.debug("Saved registered service: [{}]", registeredService);
            val filename = namingStrategy.build(registeredService, FILE_EXTENSION);
            val bucket = determineBucketForRegisteredService(registeredService);
            createBucketIfNecessary(bucket);
            try (val baos = new ByteArrayOutputStream()) {
                val blobId = BlobId.of(bucket, filename);
                val blobInfo = BlobInfo.newBuilder(blobId)
                    .setMetadata(Map.of(
                        "name", registeredService.getName(),
                        "id", String.valueOf(registeredService.getId()),
                        "description", registeredService.getDescription()
                    ))
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .setContentType(MediaType.APPLICATION_JSON_VALUE)
                    .build();
                serializer.to(baos, registeredService);
                cloudStorage.create(blobInfo, baos.toByteArray());
                LOGGER.debug("Added service [{}] to [{}] in bucket [{}]", registeredService.getName(), filename, bucket);
                return registeredService;
            }
        });
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val filename = namingStrategy.build(registeredService, FILE_EXTENSION);
        val bucket = determineBucketForRegisteredService(registeredService);
        createBucketIfNecessary(bucket);
        LOGGER.debug("Deleting service [{}] @ [{}] in bucket [{}]", registeredService.getName(), filename, bucket);
        return cloudStorage.delete(bucket, filename);
    }

    @Override
    public void deleteAll() {
        val buckets = cloudStorage.list(Storage.BucketListOption.prefix(BUCKET_PREFIX));
        for (val bucket : buckets.iterateAll()) {
            for (val blob : cloudStorage.list(bucket.getName()).iterateAll()) {
                blob.delete();
            }
        }
    }

    @Override
    public Collection<RegisteredService> load() {
        val buckets = cloudStorage.list(Storage.BucketListOption.prefix(BUCKET_PREFIX));
        return buckets.streamAll()
            .map(bucket -> {
                val bucketName = bucket.getName();
                LOGGER.debug("Loading services from bucket [{}]", bucketName);
                return cloudStorage.list(bucketName).streamAll()
                    .map(blob -> {
                        val content = blob.getContent();
                        val body = new String(content, StandardCharsets.UTF_8);
                        return serializer.from(body);
                    })
                    .toList();
            })
            .flatMap(Collection::stream)
            .peek(this::invokeServiceRegistryListenerPostLoad)
            .collect(Collectors.toSet());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val buckets = cloudStorage.list(Storage.BucketListOption.prefix(BUCKET_PREFIX));
        for (val bucket : buckets.iterateAll()) {
            LOGGER.debug("Locating service with id [{}] in bucket [{}]", id, bucket.getName());
            for (val blob : cloudStorage.list(bucket.getName()).iterateAll()) {
                if (blob.getName().contains(String.valueOf(id))) {
                    val content = blob.getContent();
                    val body = new String(content, StandardCharsets.UTF_8);
                    return serializer.from(body);
                }
            }
        }
        return null;
    }

    protected void createBucketIfNecessary(final String bucketName) {
        val bucket = cloudStorage.get(bucketName);
        if (bucket == null) {
            LOGGER.info("Creating bucket [{}]", bucketName);
            cloudStorage.create(BucketInfo.newBuilder(bucketName).build());
        }
    }

    protected String determineBucketForRegisteredService(final RegisteredService registeredService) {
        return BUCKET_PREFIX + Strings.CI.replace(registeredService.getFriendlyName(), " ", "-").toLowerCase(Locale.ENGLISH);
    }
}
