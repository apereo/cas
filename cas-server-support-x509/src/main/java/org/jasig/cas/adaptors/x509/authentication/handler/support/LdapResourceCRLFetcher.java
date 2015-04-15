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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.CompressionUtils;
import org.springframework.core.io.ByteArrayResource;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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

    /** Authentication mode for ldap. **/
    protected String securityAuthentication;

    /** Authentication principal for ldap. **/
    protected String securityPrincipal;

    /** Authentication credentials for ldap. **/
    protected String securityCredentials;

    /** object name to look up in the context. **/
    protected String objectName;

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

    /**
     * This can be one of the following strings:
     * "none", "simple", sasl_mech, where sasl_mech is a
     * space-separated list of SASL mechanism names
     * @param securityAuthentication the authn mode
     */
    public void setSecurityAuthentication(final String securityAuthentication) {
        this.securityAuthentication = securityAuthentication;
    }

    public void setSecurityPrincipal(final String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public void setSecurityCredentials(final String securityCredential) {
        this.securityCredentials = securityCredential;
    }

    public void setObjectName(final String objectName) {
        this.objectName = objectName;
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
        DirContext ctx = null;
        try {
            final String ldapURL = r.toString();
            logger.debug("Fetching CRL from ldap {}", ldapURL);

            final Map<String, String> env = configureLdapDirectoryContext(ldapURL);

            logger.debug("Establishing a connection to {}", ldapURL);
            ctx = new InitialDirContext((Hashtable) env);

            logger.debug("Connected to {}", ldapURL);
            if (StringUtils.isNotBlank(this.objectName)) {
                logger.debug("Retrieving object {}", this.objectName);
                ctx = (DirContext) ctx.lookup(this.objectName);
            }

            logger.debug("Retrieving certificate revocation list attribute {}",
                    this.certificateRevocationListAttributeName);
            final Attributes attrs = ctx.getAttributes("");
            final Attribute aval = attrs.get(this.certificateRevocationListAttributeName);
            if (aval != null) {
                final byte[] val = (byte[]) aval.get();
                if (val == null || val.length == 0) {
                    throw new CertificateException("Empty attribute. Can not download CRL from: " + ldapURL);
                }
                final byte[] decoded64 = CompressionUtils.decodeBase64ToByteArray(val);
                logger.debug("Retrieved CRL from ldap as byte array decoded in base64. Fetching...");
                return super.fetch(new ByteArrayResource(decoded64));
            }
            throw new CertificateException("Attribute not found. Can not retrieve CRL from attribute: "
                    + this.certificateRevocationListAttributeName);
        } catch (final AuthenticationException | OperationNotSupportedException e) {
            logger.error(e.getMessage(), e);
            throw new CertificateException(e);
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Configure ldap directory context.
     *
     * @param ldapURL the ldap uRL
     * @return the map of settings for authentication
     */
    protected Map<String, String> configureLdapDirectoryContext(final String ldapURL) {
        final Map<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        if (StringUtils.isNotBlank(this.securityAuthentication)) {
            env.put(Context.SECURITY_AUTHENTICATION, this.securityAuthentication);
        }
        if (StringUtils.isNotBlank(this.securityPrincipal)) {
            env.put(Context.SECURITY_PRINCIPAL, this.securityPrincipal);
        }
        if (StringUtils.isNotBlank(this.securityCredentials)) {
            env.put(Context.SECURITY_CREDENTIALS, this.securityCredentials);
        }
        return env;
    }
}
