/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.adaptors.generic.remote;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

/**
 * Checks if the remote address is in the range of allowed addresses.
 *
 * @author David Harrison
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public final class RemoteAddressAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final int HEX_RIGHT_SHIFT_COEFFICIENT = 0xff;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** The network netmask. */
    @NotNull
    private InetAddress inetNetmask;

    /** The network base address. */
    @NotNull
    private InetAddress inetNetwork;

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final RemoteAddressCredential c = (RemoteAddressCredential) credential;
        try {
            final InetAddress inetAddress = InetAddress.getByName(c.getRemoteAddress().trim());
            if (containsAddress(this.inetNetwork, this.inetNetmask, inetAddress)) {
                return new DefaultHandlerResult(this, c, this.principalFactory.createPrincipal(c.getId()));
            }
        } catch (final UnknownHostException e) {
            logger.debug("Unknown host {}", c.getRemoteAddress());
        }
        throw new FailedLoginException(c.getRemoteAddress() + " not in allowed range.");
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RemoteAddressCredential;
    }

    /**
     * The following code is from the Apache Software Foundations's Lenya project
     * See InetAddressUtil.java
     * Distributed under the Apache 2.0 software license
     */

    /**
     * Checks if a subnet contains a specific IP address.
     *
     * @param network The network address.
     * @param netmask The subnet mask.
     * @param ip The IP address to check.
     * @return A boolean value.
     */
    private boolean containsAddress(final InetAddress network, final InetAddress netmask, final InetAddress ip) {
        logger.debug("Checking IP address: {} in {} by {}", ip, network, netmask);

        final byte[] networkBytes = network.getAddress();
        final byte[] netmaskBytes = netmask.getAddress();
        final byte[] ipBytes = ip.getAddress();

        /* check IPv4/v6-compatibility or parameters: */
        if(networkBytes.length != netmaskBytes.length
                || netmaskBytes.length != ipBytes.length) {
            logger.debug("Network address {}, subnet mask {} and/or host address {}"
                    + " have different sizes! (return false ...)", network, netmask, ip);
            return false;
        }

        /* Check if the masked network and ip addresses match: */
        for(int i=0; i < netmaskBytes.length; i++) {
            final int mask = netmaskBytes[i] & HEX_RIGHT_SHIFT_COEFFICIENT;
            if((networkBytes[i] & mask) != (ipBytes[i] & mask)) {
                logger.debug("{} is not in {}/{}", ip, network, netmask);
                return false;
            }
        }
        logger.debug("{} is in {}/{}", ip, network, netmask);
        return true;
    }

    /**
     * @param ipAddressRange the IP address range that should be allowed trusted logins     *
     */
    public void setIpNetworkRange(final String ipAddressRange) {

        if(ipAddressRange != null) {

            final String[] splitAddress = ipAddressRange.split("/");

            if (splitAddress.length == 2) {
                // A valid ip address/netmask was supplied parse values
                final String network = splitAddress[0].trim();
                final String netmask = splitAddress[1].trim();

                try {
                    this.inetNetwork = InetAddress.getByName(network);
                    logger.debug("InetAddress network: {}", this.inetNetwork.toString());
                } catch (final UnknownHostException e) {
                    logger.error("The network address was not valid: {}", e.getMessage());
                }

                try {
                    this.inetNetmask = InetAddress.getByName(netmask);
                    logger.debug("InetAddress netmask: {}", this.inetNetmask.toString());
                } catch (final UnknownHostException e) {
                    logger.error("The network netmask was not valid: {}", e.getMessage());
                }
            }
        }
    }
}
