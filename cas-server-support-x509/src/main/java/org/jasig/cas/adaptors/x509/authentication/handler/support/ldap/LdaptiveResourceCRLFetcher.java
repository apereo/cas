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

package org.jasig.cas.adaptors.x509.authentication.handler.support.ldap;

import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import javax.validation.constraints.NotNull;
import org.jasig.cas.adaptors.x509.authentication.handler.support.ResourceCRLFetcher;
import org.jasig.cas.util.CompressionUtils;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.springframework.core.io.ByteArrayResource;

/**
 * Fetches a CRL from an LDAP instance.
 * @author Daniel Fisher
 * @since 4.1
 */
public class LdaptiveResourceCRLFetcher extends ResourceCRLFetcher {

    /** Search exec that looks for the attribute. */
    protected final SearchExecutor searchExecutor;

    /** The connection config to prep for connections. **/
    protected final ConnectionConfig connectionConfig;

    /**
     * Instantiates a new Ldap resource cRL fetcher.

     * @param connectionConfig the connection configuration
     * @param searchExecutor the search executor
     */
    public LdaptiveResourceCRLFetcher(
            @NotNull final ConnectionConfig connectionConfig,
            @NotNull final SearchExecutor searchExecutor) {
        this.connectionConfig = connectionConfig;
        this.searchExecutor = searchExecutor;
    }

    @Override
    protected X509CRL fetchInternal(final Object r) throws Exception {
        if (r.toString().toLowerCase().startsWith("ldap")) {
            return fetchCRLFromLdap(r);
        }
        return super.fetchInternal(r);
    }

    /**
     * Downloads a CRL from given LDAP url.
     *
     * @param r the resource that is the ldap url.
     * @return the x 509 cRL
     * @throws Exception if connection to ldap fails, or attribute to get the revocation list is unavailable
     */
    protected X509CRL fetchCRLFromLdap(final Object r) throws Exception {
        try {
            final String ldapURL = r.toString();
            logger.debug("Fetching CRL from ldap {}", ldapURL);

            final Response<SearchResult> result = performLdapSearch(ldapURL);
            if (result.getResultCode() == ResultCode.SUCCESS) {
                final LdapEntry entry = result.getResult().getEntry();
                final LdapAttribute attribute = entry.getAttribute();

                logger.debug("Located entry [{}]. Retrieving first attribute [{}]",
                        entry, attribute);
                return fetchX509CRLFromAttribute(attribute);
            } else {
                logger.debug("Failed to execute the search [{}]", result);
            }

            throw new CertificateException("Failed to establish a connection ldap and search.");

        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
            throw new CertificateException(e);
        }
    }


    /**
     * Gets x509 cRL from attribute. Retrieves the binary attribute value,
     * decodes it to base64, and fetches it as a byte-array resource.
     *
     * @param aval the attribute, which may be null if it's not found
     * @return the x 509 cRL from attribute
     * @throws Exception if attribute not found or could could not be decoded.
     */
    protected X509CRL fetchX509CRLFromAttribute(final LdapAttribute aval) throws Exception {
        if (aval != null) {
            final byte[] val = aval.getBinaryValue();
            if (val == null || val.length == 0) {
                throw new CertificateException("Empty attribute. Can not download CRL from ldap");
            }
            final byte[] decoded64 = CompressionUtils.decodeBase64ToByteArray(val);
            if (decoded64 == null) {
                throw new CertificateException("Could not decode the attribute value to base64");
            }
            logger.debug("Retrieved CRL from ldap as byte array decoded in base64. Fetching...");
            return super.fetch(new ByteArrayResource(decoded64));
        }
        throw new CertificateException("Attribute not found. Can not retrieve CRL");
    }

    /**
     * Executes an LDAP search against the supplied URL.
     *
     * @param ldapURL to search
     * @return search result
     * @throws LdapException if an error occurs performing the search
     */
    protected Response<SearchResult> performLdapSearch(final String ldapURL) throws LdapException {
        final ConnectionFactory connectionFactory = prepareConnectionFactory(ldapURL);
        return this.searchExecutor.search(connectionFactory);
    }

    /**
     * Prepare a new LDAP connection.
     *
     * @param ldapURL the ldap uRL
     * @return connection factory
     */
    protected ConnectionFactory prepareConnectionFactory(final String ldapURL) {
        final ConnectionConfig cc = ConnectionConfig.newConnectionConfig(this.connectionConfig);
        cc.setLdapUrl(ldapURL);
        return new DefaultConnectionFactory(cc);
    }
}
