package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.support.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth-rest")
@Getter
@Setter
@Accessors(chain = true)
public class RestfulGoogleAuthenticatorMultifactorProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = 4518622579150572559L;

    /**
     * Endpoint url of the REST resource used for tokens that are kept to prevent replay attacks.
     */
    @RequiredProperty
    private String tokenUrl;
}
