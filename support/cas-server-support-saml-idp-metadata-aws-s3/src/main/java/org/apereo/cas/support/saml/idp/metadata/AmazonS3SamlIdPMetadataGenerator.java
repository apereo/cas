package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AmazonS3SamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    private final S3Client s3Client;

    private final String bucketName;

    public AmazonS3SamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                            final S3Client s3Client, final String bucketName) {
        super(context);
        this.s3Client = s3Client;
        this.bucketName = SpringExpressionLanguageValueResolver.getInstance().resolve(bucketName);
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val bucketNameToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService, this.bucketName, s3Client);
        if (s3Client.listBuckets(ListBucketsRequest.builder().build())
            .buckets().stream().noneMatch(b -> b.name().equalsIgnoreCase(bucketNameToUse))) {
            LOGGER.trace("Bucket [{}] does not exist. Creating...", bucketNameToUse);
            val bucket = s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketNameToUse).build());
            LOGGER.debug("Created bucket [{}]", bucket.location());
        }
        return AmazonS3SamlIdPMetadataUtils.putSamlIdPMetadataIntoBucket(s3Client, doc, bucketNameToUse);
    }
    
}

