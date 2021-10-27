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
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.AbstractLdapPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.util.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.util.StringUtils;

/**
 * Resolves the IP address to some address in LDAP.
 * 
 * @author David Harrison
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RemoteIpLookupCredentialsToPrincipalResolver extends AbstractLdapPersonDirectoryCredentialsToPrincipalResolver {
    
    /** The IP address formats supported */
    private static final String ADDR_STANDARD = "standard";
    
    private static final String ADDR_EDIR87 = "edirectory87";
    
    private final Logger log = LoggerFactory.getLogger(getClass()); 
    
    /** The format that the IP address is stored in the LDAP source - 'standard' is default */
    @NotNull
    private String ipAddressFormat = ADDR_STANDARD;

    protected String extractPrincipalId(final Credentials credentials) {
        final RemoteAddressCredentials c = (RemoteAddressCredentials) credentials;
        final String formattedIpAddress = getFormattedIpAddress(c.getRemoteAddress().trim());      
        
        if (!StringUtils.hasText(formattedIpAddress)) {
            return null;
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Original IP address: " + c.getRemoteAddress());
            log.debug("Formatted IP address: " + formattedIpAddress);
        }
        
        final String attributeId = getAttributeIds()[0];
        final List principalList = this.getLdapTemplate().search(
            getSearchBase(), LdapUtils.getFilterWithValues(getFilter(), formattedIpAddress), getSearchControls(),
            new AttributesMapper() {
                public Object mapFromAttributes(final Attributes attrs)
                    throws NamingException {
                    final Attribute attribute = attrs.get(attributeId);
                    return attribute == null ? null : attribute.get();
                }

            });

        if (principalList.isEmpty()) {
            log.debug("LDAP search returned zero results.");
            return null;
        }
        if (principalList.size() > 1) {
            log.error("LDAP search returned multiple results "
                + "for filter \"" + getFilter() + "\", "
                + "which is not allowed.");

            return null;
        }
        return (String) principalList.get(0);
    }

    public boolean supports(final Credentials credentials) {
        return credentials.getClass().equals(RemoteAddressCredentials.class);
    }

    /**
     * Method to return the principal username of the trusted LDAP user. 
     * @param remoteAddress the remote unformatted IP address.
     * @return the formatted IP address.
     */
    private String getFormattedIpAddress(final String remoteAddress) {
        try {
            final String formattedAddress = InetAddress.getByName(remoteAddress).getHostAddress();
            
            if(this.ipAddressFormat.compareTo(ADDR_EDIR87)==0) {            
               /**
                * Number format convention
                * byte 0 = Address type (0 = IPX, 1 = IP)
                * byte 1 = Separator
                * byte 2+ = address
                * Used to get an IP address (type = 1) from a Novell eDirectory LDAP return statement
                */         
                String networkAddress = "\\31\\23";
                // Turn ip (192.168.0.1) into a byte array, covert each into a byte String
                String[] octets = formattedAddress.replace('.','_').split("_");
                
                for (int i = 0; i < octets.length; i++) {
                    String octet = octets[i];
                    networkAddress += "\\" + getHexadecimal(octet);                
                }
               return networkAddress;            
            }
            
            return formattedAddress;
        } catch (final UnknownHostException e) {
            log.error(e.toString(),e);
            return null;
        }
    }
    
    /**
     * Method to convert a byte into a two+ digit hex string.
     * @param octet the int value to convert to a hex string
     * @return the maximum number of results.
     */
    private String getHexadecimal(final String octet) {        
        final int ipOctet = Integer.parseInt(octet);
        String hex = Integer.toString(ipOctet, 16);
        while (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }
    
    /**
     * @param ipAddressFormat The format in which this IP address is stored in the LDAP source.
     */
    public void setIpAddressFormat(final String ipAddressFormat) {
        this.ipAddressFormat = ipAddressFormat;
    }
}
