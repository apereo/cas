package org.apereo.cas.configuration.model.support.mfa;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GlobalMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GlobalMultifactorAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 5426522468929733907L;

    /**
     * MFA can be triggered for all applications and users regardless of individual settings.
     * This setting holds the value of an MFA provider that shall be activated for all requests,
     * regardless. Multiple provider identifiers can be specified here via a comma-separated syntax
     * which may force CAS to launch into a provider selection and resolution flow.
     */
    private String globalProviderId;
}
