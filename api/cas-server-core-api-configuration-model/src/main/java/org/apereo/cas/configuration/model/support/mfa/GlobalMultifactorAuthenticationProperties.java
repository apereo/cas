package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("GlobalMultifactorAuthenticationProperties")
public class GlobalMultifactorAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 5426522468929733907L;

    /**
     * MFA can be triggered for all applications and users regardless of individual settings.
     * This setting holds the value of an MFA provider that shall be activated for all requests,
     * regardless.
     */
    private String globalProviderId;
}
