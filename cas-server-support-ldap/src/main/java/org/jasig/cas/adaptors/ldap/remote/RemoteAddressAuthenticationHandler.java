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
package org.jasig.cas.adaptors.ldap.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Checks if the remote address is in the range of allowed addresses.
 * 
 * @author David Harrison
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RemoteAddressAuthenticationHandler implements
    AuthenticationHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** The network netmask */
    @NotNull
    private InetAddress inetNetmask = null;
    
    /** The network base address */
    @NotNull
    private InetAddress inetNetwork = null;

    public boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        final RemoteAddressCredentials c = (RemoteAddressCredentials) credentials;
        try {
            final InetAddress inetAddress = InetAddress.getByName(c.getRemoteAddress().trim());
            return containsAddress(this.inetNetwork, this.inetNetmask, inetAddress);
        } catch (final UnknownHostException e) {
            return false;
        }  
    }

    public boolean supports(final Credentials credentials) {
        return credentials.getClass().equals(RemoteAddressCredentials.class);
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
        if(log.isDebugEnabled()) {
            log.debug("Checking IP address: " + ip + " in " + network + " / " + netmask);
        }
        
        byte[] networkBytes = network.getAddress();
        byte[] netmaskBytes = netmask.getAddress();
        byte[] ipBytes = ip.getAddress();
        
        /* check IPv4/v6-compatibility or parameters: */
        if(networkBytes.length != netmaskBytes.length
            || netmaskBytes.length != ipBytes.length)
        {
            if(log.isDebugEnabled()) {
                log.debug("Network address " + network + ", subnet mask "
                     + netmask + " and/or host address " + ip
                     + " have different sizes! (return false ...)");
            }
            return false;
        }
        
        /* Check if the masked network and ip addresses match: */
        for(int i=0; i<netmaskBytes.length; i++) {
            int mask = netmaskBytes[i] & 0xff;
            if((networkBytes[i] & mask) != (ipBytes[i] & mask)) {
                if(log.isDebugEnabled()) {
                    log.debug(ip + " is not in " + network + " / " + netmask);
                }
                return false;
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(ip + " is in " + network + " / " + netmask);
        }
        return true;
    }
    
    /**
     * @param ipAddressRange the IP address range that should be allowed trusted logins     * 
     */
    public void setIpNetworkRange(final String ipAddressRange) {
            
        if(ipAddressRange != null) {
        
            final String[] splitAddress = ipAddressRange.split( "/" );

            if (splitAddress.length == 2 ) {
                // A valid ip address/netmask was supplied parse values
                final String network = splitAddress[0].trim();
                final String netmask = splitAddress[1].trim();
        
                try {
                    this.inetNetwork = InetAddress.getByName(network);
                    if(log.isDebugEnabled()) {
                        log.debug("InetAddress network: " + this.inetNetwork.toString());
                    }                    
                } catch (final UnknownHostException e ) {
                    log.error("The network address was not valid: " + e.getMessage());
                }
                
                try {
                    this.inetNetmask = InetAddress.getByName(netmask);
                    if(log.isDebugEnabled()) {
                        log.debug("InetAddress netmask: " + this.inetNetmask.toString());
                    }                    
                } catch (final UnknownHostException e ) {
                    log.error("The network netmask was not valid: " + e.getMessage());
                }
            }
        }
    }
}
