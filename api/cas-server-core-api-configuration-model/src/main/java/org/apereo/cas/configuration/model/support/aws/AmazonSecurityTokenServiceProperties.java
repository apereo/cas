package org.apereo.cas.configuration.model.support.aws;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link AmazonSecurityTokenServiceProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws")
@Accessors(chain = true)
public class AmazonSecurityTokenServiceProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = 5426637051495147084L;

    /**
     * Attribute name that must be found and resolved
     * for the principal to authorize the user to
     * proceed with obtaining credentials.
     */
    private String principalAttributeName;

    /**
     * Attribute value, defined as a regex pattern
     * that must be found and resolved
     * for the principal to authorize the user to
     * proceed with obtaining credentials.
     */
    @RegularExpressionCapable
    private String principalAttributeValue;

    /**
     * When set to {@code true}, credentials will be obtained based on
     * roles as attributes resolved for the user. Typically, you could use roles
     * within your account or for cross-account access.
     * <p>
     * When set to {@code true}, the {@link #getPrincipalAttributeName()}
     * must contain {@code roleArn}s as values.
     */
    private boolean rbacEnabled;
}
