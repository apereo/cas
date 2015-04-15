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

package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.Hashtable;
import java.util.Map;

/**
 * Fetches a CRL from an LDAP instance.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class LdapResourceCRLFetcher extends ResourceCRLFetcher {

    /** The Certificate revocation list attribute name.*/
    protected final String certificateRevocationListAttributeName;

    /**
     * Instantiates a new Ldap resource cRL fetcher.
     */
    public LdapResourceCRLFetcher() {
        this("certificateRevocationList;binary");
    }

    /**
     * Instantiates a new Ldap resource cRL fetcher.
     *
     * @param certificateRevocationListAttributeName the certificate revocation list attribute name
     */
    public LdapResourceCRLFetcher(final String certificateRevocationListAttributeName) {
        this.certificateRevocationListAttributeName = certificateRevocationListAttributeName;
    }

    @Override
    protected X509CRL fetchInternal(final Object r) throws Exception {
        if (r.toString().toLowerCase().startsWith("ldap")) {
            return fetchCRLFromLdap(r);
        }
        return super.fetchInternal(r);
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * <code>ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com</code>
     *
     * @param r the resource
     * @return the x 509 cRL
     * @throws Exception if connection to ldap fails, or attribute to get the revocation list is unavailable
     */
    protected X509CRL fetchCRLFromLdap(final Object r) throws Exception {

        final String ldapURL = r.toString();
        logger.debug("Fetching CRL from ldap {}", ldapURL);

        final Map<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        logger.debug("Establishing a connection to {}", ldapURL);
        final DirContext ctx = new InitialDirContext((Hashtable) env);
        logger.debug("Retrieving certificate revocation list attribute {}",
                this.certificateRevocationListAttributeName);
        final Attribute aval = ctx.getAttributes("").get(this.certificateRevocationListAttributeName);
        final byte[] val = (byte[]) aval.get();
        if (val == null || val.length == 0) {
            throw new CertificateException("Can not download CRL from: " + ldapURL);
        }
        ctx.close();
        logger.debug("Retrieved CRL from ldap as byte array. Fetching...");
        return super.fetch(new ByteArrayResource(val));
    }
}
