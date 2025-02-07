package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link AmazonSesProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws-ses", automated = false)
@Accessors(chain = true)

public class AmazonSesProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = -1202529110472766098L;

    /**
     * Configures source ARN. Used only for sending authorization.
     * The ARN of the identity that is associated with the sending authorization policy that permits you to send for the email address specified.
     */
    private String sourceArn;

    /**
     * Configures configuration set name.
     * Configuration sets let you create groups of rules that you can apply to the emails you send using Amazon SES.
     */
    private String configurationSetName;
}
