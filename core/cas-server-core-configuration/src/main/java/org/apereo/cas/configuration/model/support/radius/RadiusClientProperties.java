package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * This is {@link RadiusClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-radius")
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

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(final String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(final int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getAuthenticationPort() {
        return authenticationPort;
    }

    public void setAuthenticationPort(final int authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public int getAccountingPort() {
        return accountingPort;
    }

    public void setAccountingPort(final int accountingPort) {
        this.accountingPort = accountingPort;
    }

    public String getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(final String inetAddress) {
        this.inetAddress = inetAddress;
    }

}
