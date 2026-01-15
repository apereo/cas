package org.apereo.cas.configuration.model.core.audit;

import module java.base;
import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AuditAmazonFirehoseProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-audit-aws-firehose")
@Getter
@Setter
@Accessors(chain = true)
public class AuditAmazonFirehoseProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = -5791277338807046269L;

    /**
     * The Firehose delivery stream that writes logs to a destination
     * such as an S3 bucket.
     */
    @RequiredProperty
    private String deliveryStreamName;

    /**
     * Make storage requests asynchronously.
     */
    private boolean asynchronous = true;
}

