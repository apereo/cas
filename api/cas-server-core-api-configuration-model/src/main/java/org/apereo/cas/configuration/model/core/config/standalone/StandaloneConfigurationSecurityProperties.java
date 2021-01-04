package org.apereo.cas.configuration.model.core.config.standalone;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link StandaloneConfigurationSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-configuration", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class StandaloneConfigurationSecurityProperties implements Serializable {
    private static final long serialVersionUID = 8571848605614437022L;

    /**
     * Algorithm to use when deciphering settings.
     */
    private String alg;

    /**
     * Security provider to use when deciphering settings.
     */
    private String provider;

    /**
     * Total number of iterations to use when deciphering settings.
     */
    private long iteration;

    /**
     * Secret key/password to use when deciphering settings.
     */
    private String psw;
}
