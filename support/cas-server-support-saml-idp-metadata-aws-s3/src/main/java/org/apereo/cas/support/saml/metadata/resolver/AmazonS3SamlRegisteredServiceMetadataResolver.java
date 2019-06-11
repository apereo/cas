package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
    private final transient AmazonS3 s3Client;
    private final String bucketName;

    public AmazonS3SamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                         final OpenSamlConfigBean configBean,
                                                         final AmazonS3 s3Client) {
        super(samlIdPProperties, configBean);
        this.bucketName = samlIdPProperties.getMetadata().getAmazonS3().getBucketName();
        this.s3Client = s3Client;
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try {
            val result = s3Client.listObjectsV2(bucketName);
            val objects = result.getObjectSummaries();
            LOGGER.debug("Located [{}] S3 object(s) from bucket [{}]", objects.size(), bucketName);

            return objects.stream()
                .map(obj -> {
                    val objectKey = obj.getKey();
                    LOGGER.debug("Fetching object [{}] from bucket [{}]", objectKey, bucketName);
                    val object = s3Client.getObject(obj.getBucketName(), objectKey);
                    try (val is = object.getObjectContent()) {
                        val document = new SamlMetadataDocument();
                        document.setId(System.nanoTime());
                        document.setName(objectKey);
                        val objectMetadata = object.getObjectMetadata();
                        if (objectMetadata != null) {
                            document.setSignature(objectMetadata.getUserMetaDataOf("signature"));
                            if (StringUtils.isNotBlank(document.getSignature())) {
                                LOGGER.debug("Found metadata signature as part of object metadata for [{}] from bucket [{}]", objectKey, bucketName);
                            }
                        }
                        document.setValue(IOUtils.toString(is));
                        return buildMetadataResolverFrom(service, document);
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("awss3://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    @SneakyThrows
    public void saveOrUpdate(final SamlMetadataDocument document) {
        val is = new ByteArrayInputStream(document.getValue().getBytes(StandardCharsets.UTF_8));
        val metadata = new ObjectMetadata();
        metadata.getUserMetadata().put("signature", document.getSignature());
        this.s3Client.putObject(bucketName, document.getName(), is, metadata);
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service) && !s3Client.listBuckets().isEmpty();
    }
}
