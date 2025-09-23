package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcCibaProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcCibaProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 313328615694269276L;

    /**
     * Hard timeout to kill the ID token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT5M";

    /**
     * The storage object name used and created by CAS to hold CIBA requests
     * in the backing ticket registry implementation.
     */
    private String storageName = "oidcCibaRequestsCache";
    
    /**
     * Control CIBA notification settings
     * to authenticate the user via email, etc.
     */
    @NestedConfigurationProperty
    private OidcCibaVerificationProperties verification = new OidcCibaVerificationProperties();
}
