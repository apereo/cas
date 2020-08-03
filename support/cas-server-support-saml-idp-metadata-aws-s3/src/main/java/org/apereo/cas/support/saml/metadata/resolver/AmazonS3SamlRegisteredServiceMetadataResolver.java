package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.LoggingUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link AmazonS3SamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AmazonS3SamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private final transient S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                         final OpenSamlConfigBean configBean,
                                                         final S3Client s3Client) {
        super(samlIdPProperties, configBean);
        this.bucketName = samlIdPProperties.getMetadata().getAmazonS3().getBucketName();
        this.s3Client = s3Client;
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try {
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
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("awss3://");
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    @SneakyThrows
    public void saveOrUpdate(final SamlMetadataDocument document) {
        val request = PutObjectRequest.builder().bucket(bucketName)
            .key(document.getName())
            .metadata(Map.of("signature", document.getSignature()))
            .build();
        s3Client.putObject(request, RequestBody.fromString(document.getValue()));
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service) && s3Client.listBuckets().hasBuckets();
    }
}
