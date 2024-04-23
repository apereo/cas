package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcLogoutProperties}.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcLogoutProperties")
public class OidcLogoutProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4988981831781991817L;

    /**
     * Whether the back-channel logout is supported.
     */
    private boolean backchannelLogoutSupported = true;

    /**
     * Whether the front-channel logout is supported.
     */
    private boolean frontchannelLogoutSupported = true;
}
