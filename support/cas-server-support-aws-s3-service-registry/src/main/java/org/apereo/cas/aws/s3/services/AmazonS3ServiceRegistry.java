package org.apereo.cas.aws.s3.services;

import module java.base;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * This is {@link AmazonS3ServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class AmazonS3ServiceRegistry extends AbstractServiceRegistry {
    /**
     * Bucket name prefix.
     */
    static final String BUCKET_NAME_PREFIX = "cas";

    private static final Pattern REGISTERED_SERVICE_BUCKET_PATTERN = Pattern.compile(BUCKET_NAME_PREFIX + "-.*-\\d+");

    private final S3Client s3Client;

    private final StringSerializer<RegisteredService> registeredServiceSerializer;

    public AmazonS3ServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                   final S3Client s3Client) {
        super(applicationContext, serviceRegistryListeners);
        this.s3Client = s3Client;
        this.registeredServiceSerializer = new RegisteredServiceJsonSerializer(applicationContext);
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            rs.assignIdIfNecessary();
            LOGGER.trace("Saving registered service [{}]", rs);
            invokeServiceRegistryListenerPreSave(rs);
            val bucketNameToUse = determineBucketName(rs);
            if (s3Client.listBuckets(ListBucketsRequest.builder().build())
                .buckets().stream().noneMatch(b -> b.name().equalsIgnoreCase(bucketNameToUse))) {
                LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketNameToUse);
                val bucket = s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketNameToUse).build());
                LOGGER.debug("Created bucket [{}]", bucket.location());
            }

            val request = PutObjectRequest.builder()
                .key(String.valueOf(rs.getId()))
                .bucket(bucketNameToUse)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .metadata(Map.of(
                    "name", rs.getName(),
                    "id", String.valueOf(rs.getId()),
                    "description", rs.getDescription()))
                .build();
            val body = registeredServiceSerializer.toString(rs);
            val clientInfo = ClientInfoHolder.getClientInfo();
            s3Client.putObject(request, RequestBody.fromString(body));
            LOGGER.trace("Saved registered service [{}]", rs);
            publishEvent(new CasRegisteredServiceSavedEvent(this, rs, clientInfo));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return rs;
    }

    @Override
    public void deleteAll() {
        val buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
        buckets.stream()
            .filter(AmazonS3ServiceRegistry::getRegisteredServiceBucketPredicate)
            .forEach(bucket -> {
                val objects = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket.name()).build());
                objects.contents().forEach(object -> s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket.name())
                    .key(object.key())
                    .build()));
                s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucket.name()).build());
            });
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            LOGGER.trace("Deleting registered service [{}]", registeredService);
            val buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
            val result = buckets
                .stream()
                .filter(getRegisteredServiceBucketPredicate(registeredService.getId()))
                .findFirst();
            if (result.isPresent()) {
                val bucket = result.get();
                val objects = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket.name()).build());
                objects.contents().forEach(object -> s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket.name())
                    .key(object.key())
                    .build()));
                val bucketName = determineBucketName(registeredService);
                val clientInfo = ClientInfoHolder.getClientInfo();
                s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
                LOGGER.trace("Deleted registered service [{}]", registeredService);
                publishEvent(new CasRegisteredServiceDeletedEvent(this, registeredService, clientInfo));
                return true;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public Collection<RegisteredService> load() {
        val buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
        return buckets
            .stream()
            .filter(AmazonS3ServiceRegistry::getRegisteredServiceBucketPredicate)
            .map(this::fetchRegisteredServiceFromBucket)
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
        return buckets
            .stream()
            .filter(getRegisteredServiceBucketPredicate(id))
            .map(this::fetchRegisteredServiceFromBucket)
            .findFirst()
            .orElse(null);
    }

    @Override
    public long size() {
        val buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
        return buckets
            .stream()
            .filter(AmazonS3ServiceRegistry::getRegisteredServiceBucketPredicate)
            .count();
    }

    private RegisteredService fetchRegisteredServiceFromBucket(final Bucket bucket) {
        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket.name()).build());
        val objects = result.contents();
        LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucket.name());
        val objectKey = objects.getFirst().key();
        LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucket.name());
        val object = s3Client.getObject(GetObjectRequest.builder().bucket(bucket.name()).key(objectKey).build());
        return registeredServiceSerializer.from(object);
    }

    /**
     * Determine whether a bucket holds a registered service.
     * <p>
     * Only buckets named by {@link #determineBucketName(RegisteredService)}, that is {@code cas-<name>-<id>},
     * belong to this registry. Matching on the {@code cas} prefix alone also claims unrelated buckets that
     * happen to start with it, such as a SAML metadata bucket, which would then be read as registered services
     * and emptied and removed by {@link #deleteAll()}.
     *
     * @param bucket the bucket
     * @return true if the bucket holds a registered service
     */
    private static boolean getRegisteredServiceBucketPredicate(final Bucket bucket) {
        return REGISTERED_SERVICE_BUCKET_PATTERN.matcher(bucket.name()).matches();
    }

    /**
     * Determine whether a bucket holds the registered service with the given identifier.
     * <p>
     * The identifier must be the last segment of the bucket name. A bucket whose name merely contains the
     * identifier, such as the bucket of service {@code 10} when looking for service {@code 1}, does not match.
     *
     * @param id the registered service identifier
     * @return the bucket predicate
     */
    private static Predicate<Bucket> getRegisteredServiceBucketPredicate(final long id) {
        return bucket -> getRegisteredServiceBucketPredicate(bucket) && bucket.name().endsWith("-" + id);
    }

    private static String determineBucketName(final RegisteredService registeredService) {
        return (BUCKET_NAME_PREFIX + '-' + registeredService.getName() + '-' + registeredService.getId()).toLowerCase(Locale.ENGLISH);
    }
}
