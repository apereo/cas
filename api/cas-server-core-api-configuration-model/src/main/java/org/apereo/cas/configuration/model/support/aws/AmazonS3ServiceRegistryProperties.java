package org.apereo.cas.configuration.model.support.aws;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AmazonS3ServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws-s3-service-registry")
@Accessors(chain = true)
public class AmazonS3ServiceRegistryProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = -6790277338807046269L;

    /**
     * Enable path-style access for S3 buckets over virtual-hosted–style access.
     * In Amazon S3, path-style URLs use the following format:
     * {@code https://s3.region-code.amazonaws.com/bucket-name/key-name}
     * Currently, Amazon S3 supports both virtual-hosted–style and path-style URL
     * access in all AWS Regions. However, path-style URLs will be discontinued in the future.
     */
    private boolean pathStyleEnabled;
}
