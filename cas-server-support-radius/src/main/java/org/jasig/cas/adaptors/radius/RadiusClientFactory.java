/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.radius;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.jradius.client.RadiusClient;

/**
 * Factory for creating RADIUS client instances.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class RadiusClientFactory {

    /** The port to do accounting on. */
    @Min(1)
    private int accountingPort = RadiusClient.defaultAcctPort;

    /** The port to do authentication on. */
    @Min(1)
    private int authenticationPort = RadiusClient.defaultAuthPort;

    /** Socket timeout in seconds. */
    @Min(0)
    private int socketTimeout = RadiusClient.defaultTimeout;

    /** RADIUS server network address. */
    @NotNull
    private InetAddress inetAddress;

    /** The shared secret to send to the RADIUS server. */
    @NotNull
    private String sharedSecret;

    /**
     * Sets the RADIUS server accounting port.
     *
     * @param port Accounting port number.
     */
    public void setAccountingPort(final int port) {
        this.accountingPort = port;
    }

    /**
     * Sets the RADIUS server authentication port.
     *
     * @param port Authentication port number.
     */
    public void setAuthenticationPort(final int port) {
        this.authenticationPort = port;
    }

    /**
     * Sets the RADIUS server UDP socket timeout.
     *
     * @param timeout Timeout in seconds; 0 for no timeout.
     */
    public void setSocketTimeout(final int timeout) {
        this.socketTimeout = timeout;
    }

    /**
     * RADIUS server network address.
     *
     * @param address Network address as a string.
     */
    public void setInetAddress(final String address) {
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (final UnknownHostException e) {
            throw new RuntimeException("Invalid address " + address);
        }
    }

    /**
     * RADIUS server authentication shared secret.
     *
     * @param secret Shared secret.
     */
    public void setSharedSecret(final String secret) {
        this.sharedSecret = secret;
    }

    /**
     * Creates a new RADIUS client instance using factory configuration settings.
     *
     * @return New radius client instance.
     */
    public RadiusClient newInstance() {
        return new RadiusClient(
                this.inetAddress, this.sharedSecret, this.authenticationPort, this.accountingPort, this.socketTimeout);
    }
}
