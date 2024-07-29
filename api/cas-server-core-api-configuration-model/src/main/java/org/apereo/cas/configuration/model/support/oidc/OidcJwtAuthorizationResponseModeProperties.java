package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcJwtAuthorizationResponseModeProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)

public class OidcJwtAuthorizationResponseModeProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 632228615694269276L;

    /**
     * Hard timeout to kill the JWT token and expire it.
     */
    @DurationCapable
    private String expiration = "PT60S";
}
