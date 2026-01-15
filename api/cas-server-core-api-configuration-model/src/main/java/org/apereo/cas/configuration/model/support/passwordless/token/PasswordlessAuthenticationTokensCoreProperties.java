package org.apereo.cas.configuration.model.support.passwordless.token;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PasswordlessAuthenticationTokensCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-passwordless-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationTokensCoreProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1371063350377031703L;

    /**
     * Indicate how long should the token be considered valid.
     */
    @DurationCapable
    private String expiration = "PT180S";
}
