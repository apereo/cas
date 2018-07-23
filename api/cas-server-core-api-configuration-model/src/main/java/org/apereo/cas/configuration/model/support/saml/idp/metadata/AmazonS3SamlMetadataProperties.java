package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AmazonS3SamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-aws-s3")
@Getter
@Setter
public class AmazonS3SamlMetadataProperties extends BaseAmazonWebServicesProperties {
    private static final long serialVersionUID = 352435146313504995L;

    /**
     * S3 bucket that contains metadata files.
     */
    private String bucketName;
}
