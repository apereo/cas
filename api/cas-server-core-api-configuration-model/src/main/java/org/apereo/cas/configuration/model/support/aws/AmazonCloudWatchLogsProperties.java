package org.apereo.cas.configuration.model.support.aws;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link AmazonCloudWatchLogsProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws-cloudwatch")
@Accessors(chain = true)
public class AmazonCloudWatchLogsProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = 8516821862609444134L;

    /**
     * A log group is a container that organizes and stores log streams that share the same
     * retention, monitoring, and access control settings.
     * Each log group can have a unique name, and you can think of it as a logical grouping of logs
     * that relate to a specific application, service, or environment.
     */
    @RequiredProperty
    private String logGroupName;

    /**
     * A log stream is a sequence of log events that share the same source.
     * Each log stream belongs to a log group, and you can have multiple log streams within a single log group.
     * Log streams are typically used to separate log data from different sources within the same application or service.
     */
    @RequiredProperty
    private String logStreamName;
}
