package org.apereo.cas.configuration.model.support.aws;

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
    private static final long serialVersionUID = -6790277338807046269L;
}
