package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationTriggersProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MultifactorAuthenticationTriggersProperties")
public class MultifactorAuthenticationTriggersProperties implements Serializable {

    private static final long serialVersionUID = 7411521468929733907L;

    /**
     * MFA triggers that operate based on the http request properties.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationHttpTriggerProperties http = new MultifactorAuthenticationHttpTriggerProperties();
}
