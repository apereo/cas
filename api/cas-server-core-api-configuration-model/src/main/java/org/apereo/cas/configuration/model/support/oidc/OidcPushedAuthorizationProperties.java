package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OidcPushedAuthorizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcPushedAuthorizationProperties")
public class OidcPushedAuthorizationProperties implements Serializable {

    private static final long serialVersionUID = 632228615694269276L;

    /**
     * Controls number of times a request can be used within CAS server.
     */
    private long numberOfUses = 1;

    /**
     * Hard timeout to kill the PAR token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT30S";
}
