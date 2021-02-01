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

    private static final long serialVersionUID = 7410521468929733907L;

    /**
     * MFA triggers that operate based on the http request properties.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationHttpTriggerProperties http = new MultifactorAuthenticationHttpTriggerProperties();

    /**
     * MFA can be triggered based on the results of a remote REST endpoint of your design.
     * If the endpoint is configured, CAS shall issue a POST, providing the principal and the service url.
     * The body of the response in the event of a successful 200 status code is
     * expected to be the MFA provider id which CAS should activate.
     */
    @NestedConfigurationProperty
    private RestfulMultifactorAuthenticationProperties rest = new RestfulMultifactorAuthenticationProperties();

    /**
     * Activate MFA based on properties or attributes of the principal.
     */
    @NestedConfigurationProperty
    private PrincipalAttributeMultifactorAuthenticationProperties principal =
        new PrincipalAttributeMultifactorAuthenticationProperties();

    /**
     * Activate MFA based on properties or attributes of the authentication.
     */
    @NestedConfigurationProperty
    private AuthenticationAttributeMultifactorAuthenticationProperties authentication =
        new AuthenticationAttributeMultifactorAuthenticationProperties();

    /**
     * Activate MFA based on grouper integration.
     */
    @NestedConfigurationProperty
    private GrouperMultifactorAuthenticationProperties grouper =
        new GrouperMultifactorAuthenticationProperties();

    /**
     * Activate MFA globally.
     */
    @NestedConfigurationProperty
    private GlobalMultifactorAuthenticationProperties global =
        new GlobalMultifactorAuthenticationProperties();
}
