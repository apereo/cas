package org.apereo.cas.configuration.model.core.logging;

import module java.base;
import org.apereo.cas.configuration.model.support.aws.AmazonCloudWatchLogsProperties;
import org.apereo.cas.configuration.model.support.aws.GoogleCloudLogsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link LoggingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-logging", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class LoggingProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7455171260665661949L;

    /**
     * MDC logging settings.
     */
    @NestedConfigurationProperty
    private MdcLoggingProperties mdc = new MdcLoggingProperties();

    /**
     * AWS CloudWatch logs settings.
     */
    @NestedConfigurationProperty
    private AmazonCloudWatchLogsProperties cloudwatch = new AmazonCloudWatchLogsProperties();

    /**
     * Google Cloud logs settings.
     */
    @NestedConfigurationProperty
    private GoogleCloudLogsProperties gcp = new GoogleCloudLogsProperties();
}
