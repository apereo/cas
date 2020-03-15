package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RadiusClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-radius")
@Getter
@Setter
@Accessors(chain = true)
public class RadiusClientProperties implements Serializable {

    private static final long serialVersionUID = -7961769318651312854L;

    /**
     * Server address to connect and establish a session.
     */
    @RequiredProperty
    private String inetAddress = "localhost";

    /**
     * Secret/password to use for the initial bind.
     */
    @RequiredProperty
    private String sharedSecret = "N0Sh@ar3d$ecReT";

    /**
     * Socket connection timeout in milliseconds.
     */
    private int socketTimeout;

    /**
     * The authentication port.
     */
    private int authenticationPort = 1812;

    /**
     * The accounting port.
     */
    private int accountingPort = 1813;
}
