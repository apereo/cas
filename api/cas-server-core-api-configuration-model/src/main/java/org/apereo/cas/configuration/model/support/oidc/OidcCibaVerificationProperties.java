package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link OidcCibaVerificationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcCibaVerificationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 546628615694269276L;

    /**
     * Email settings for verification.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * Number of seconds to pause and wait before sending out the verification notification
     * to the user. Essentially, this controls how long the user must wait before the
     * notification message is received via email, etc.
     */
    @DurationCapable
    private String delay = "PT5S";
}
