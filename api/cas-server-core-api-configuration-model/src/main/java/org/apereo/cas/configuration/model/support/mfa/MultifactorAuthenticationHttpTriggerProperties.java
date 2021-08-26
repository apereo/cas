package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationHttpTriggerProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MultifactorAuthenticationHttpTriggerProperties")
public class MultifactorAuthenticationHttpTriggerProperties implements Serializable {

    private static final long serialVersionUID = 5511521468929733907L;

    /**
     * MFA can be triggered for a specific authentication request,
     * provided the  request  contains a session/request attribute that indicates the required MFA authentication flow.
     * The attribute name is configurable, but its value must match the authentication provider id of an available MFA provider.
     */
    private String sessionAttribute = "authn_method";

    /**
     * MFA can be triggered for a specific authentication request,
     * provided the initial request to the CAS /login endpoint contains a request header that indicates the required MFA authentication flow.
     * The header name is configurable, but its value must match the authentication provider id of an available MFA provider.
     */
    private String requestHeader = "authn_method";

    /**
     * MFA can be triggered for a specific authentication request,
     * provided the initial request to the CAS /login endpoint contains a parameter that indicates the required MFA authentication flow.
     * The parameter name is configurable, but its value must match the authentication provider id of an available MFA provider.
     */
    private String requestParameter = "authn_method";
}
