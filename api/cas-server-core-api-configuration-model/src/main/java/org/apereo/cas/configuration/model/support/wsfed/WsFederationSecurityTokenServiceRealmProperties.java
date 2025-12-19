package org.apereo.cas.configuration.model.support.wsfed;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WsFederationSecurityTokenServiceRealmProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-ws-sts")
@Accessors(chain = true)
public class WsFederationSecurityTokenServiceRealmProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2209230334376432934L;

    /**
     * Keystore path associated with the this realm.
     */
    @RequiredProperty
    private String keystoreFile;

    /**
     * Keystore password associated with the this realm.
     */
    @RequiredProperty
    private String keystorePassword;

    /**
     * Key alias associated with the this realm.
     */
    private String keystoreAlias;

    /**
     * Key alias associated with the this realm.
     */
    @RequiredProperty
    private String keyPassword;

    /**
     * Issuer/name of the realm identified and registered with STS.
     */
    @RequiredProperty
    private String issuer = "CAS";
}
