package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * This is {@link AmazonS3SamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AmazonS3SamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private final S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                         final OpenSamlConfigBean configBean,
                                                         final S3Client s3Client) {
        super(samlIdPProperties, configBean);
        this.bucketName = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(samlIdPProperties.getMetadata().getAmazonS3().getBucketName());
        this.s3Client = s3Client;
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service,
                                                          final CriteriaSet criteriaSet) {

        LOGGER.debug("Locating S3 object(s) from bucket [{}]...", bucketName);
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(bucket -> bucket.name().equalsIgnoreCase(bucketName))) {
            LOGGER.debug("S3 bucket [{}] does not exist", bucketName);
            return new ArrayList<>();
        }

        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        val objects = result.contents();
        LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

        return objects.stream()
            .map(obj -> {
                val objectKey = obj.key();
                LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);

                try (val is = s3Client.getObject(GetObjectRequest.builder().key(objectKey).bucket(bucketName).build())) {
                    val document = new SamlMetadataDocument();
                    document.setId(System.nanoTime());
                    document.setName(objectKey);
                    val objectMetadata = is.response().metadata();
                    if (objectMetadata != null) {
                        document.setSignature(objectMetadata.get("signature"));
                        if (StringUtils.isNotBlank(document.getSignature())) {
                            LOGGER.debug("Found metadata signature as part of object metadata for [{}] from bucket [{}]", objectKey, bucketName);
                        }
                    }
                    document.setValue(IOUtils.toString(is, StandardCharsets.UTF_8));
                    return buildMetadataResolverFrom(service, document);
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith(getSourceId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(bucket -> bucket.name().equalsIgnoreCase(bucketName))) {
            LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketName);
            val bucket = s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName.toLowerCase(Locale.ENGLISH)).build());
            LOGGER.debug("Created bucket [{}]", bucket.location());
        }

        val metadata = new LinkedHashMap<String, String>();
        metadata.put("id", String.valueOf(document.getId()));
        if (StringUtils.isNotBlank(document.getSignature())) {
            metadata.put("signature", document.getSignature());
        }
        val request = PutObjectRequest.builder().bucket(bucketName)
            .key(document.getName())
            .metadata(metadata)
            .build();
        s3Client.putObject(request, RequestBody.fromString(document.getValue()));
        return document;
    }

    @Override
    public String getSourceId() {
        return "awss3://";
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service) && s3Client.listBuckets().hasBuckets();
    }

    @Override
    public List<SamlMetadataDocument> load() {
        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        return result.contents()
            .stream()
            .map(obj -> getDocumentFromBucket(obj.key()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void removeById(final long id) {
        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        result.contents()
            .stream()
            .filter(obj -> {
                val document = getDocumentFromBucket(obj.key());
                return document != null && document.getId() == id;
            })
            .forEach(obj -> s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName).key(obj.key()).build()));
    }

    @Override
    public void removeAll() {
        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        result.contents().forEach(obj -> s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName).key(obj.key()).build()));
    }

    @Override
    public void removeByName(final String name) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName).key(name).build());
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        return Optional.ofNullable(getDocumentFromBucket(name));
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        val result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        return result.contents()
            .stream()
            .map(obj -> getDocumentFromBucket(obj.key()))
            .filter(Objects::nonNull)
            .filter(doc -> doc.getId() == id)
            .findFirst();
    }

    protected @Nullable SamlMetadataDocument getDocumentFromBucket(final String key) {
        try (val is = s3Client.getObject(GetObjectRequest.builder().key(key).bucket(bucketName).build())) {
            val document = new SamlMetadataDocument();
            document.setName(key);
            val objectMetadata = is.response().metadata();
            if (objectMetadata != null) {
                document.setSignature(objectMetadata.get("signature"));
                val idValue = objectMetadata.get("id");
                if (StringUtils.isNotBlank(idValue)) {
                    document.setId(Long.parseLong(idValue));
                }
            }
            document.setValue(IOUtils.toString(is, StandardCharsets.UTF_8));
            return document;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
