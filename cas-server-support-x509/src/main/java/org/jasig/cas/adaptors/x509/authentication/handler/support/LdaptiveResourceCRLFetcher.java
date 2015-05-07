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
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.pool.AbstractConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.provider.Provider;
import org.springframework.core.io.ByteArrayResource;

import javax.naming.AuthenticationException;
import javax.naming.OperationNotSupportedException;
import javax.validation.constraints.NotNull;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;

/**
 * Fetches a CRL from an LDAP instance.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class LdaptiveResourceCRLFetcher extends ResourceCRLFetcher {

    /** Attribute name in LDAP to indicate the cert revocation list. **/
    public static final String DEFAULT_CERTIFICATE_REVOCATION_LIST_ATTRIBUTE = "certificateRevocationList;binary";

    /** The Certificate revocation list attribute name.*/
    protected final String certificateRevocationListAttributeName;

    /** The connection configuration to ldap.*/
    protected final ConnectionConfig connectionConfig;

    /** Search request to find the attribute in the ldap tree.*/
    protected final SearchRequest searchRequest;

    /** The ldap provider, defaults to {@link DefaultConnectionFactory#getProvider()}. */
    protected Provider provider = DefaultConnectionFactory.getDefaultProvider();

    /** The connection factory to prep for connections. **/
    protected ConnectionFactory connectionFactory;

    private AbstractConnectionPool connectionPool;

    /**
     * Instantiates a new Ldap resource cRL fetcher.
     * initializes the default certificate revocation attribute
     * to be {@link #DEFAULT_CERTIFICATE_REVOCATION_LIST_ATTRIBUTE}.
     * @param searchRequest the search request
     * @param connectionConfig the connection config
     */
    public LdaptiveResourceCRLFetcher(@NotNull final SearchRequest searchRequest,
                                      @NotNull final ConnectionConfig connectionConfig) {
        this(searchRequest, connectionConfig,
                DEFAULT_CERTIFICATE_REVOCATION_LIST_ATTRIBUTE);
    }

    /**
     * Instantiates a new Ldap resource cRL fetcher.
     *
     * @param searchRequest the search request
     * @param connectionConfig the connection config
     * @param certificateRevocationListAttributeName the certificate revocation list attribute name
     */
    public LdaptiveResourceCRLFetcher(@NotNull final SearchRequest searchRequest,
                                      @NotNull final ConnectionConfig connectionConfig,
                                      @NotNull final String certificateRevocationListAttributeName) {
        this.certificateRevocationListAttributeName = certificateRevocationListAttributeName;
        this.connectionConfig = connectionConfig;
        this.searchRequest = searchRequest;
    }

    @Override
    protected X509CRL fetchInternal(final Object r) throws Exception {
        if (r.toString().toLowerCase().startsWith("ldap")) {
            return fetchCRLFromLdap(r);
        }
        return super.fetchInternal(r);
    }

    public void setProvider(final Provider provider) {
        this.provider = provider;
    }

    public void setConnectionPool(final AbstractConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Downloads a CRL from given LDAP url
     *
     * @param r the resource that is the ldap url.
     * @return the x 509 cRL
     * @throws Exception if connection to ldap fails, or attribute to get the revocation list is unavailable
     */
    protected X509CRL fetchCRLFromLdap(final Object r) throws Exception {
        Connection connection = null;
        try {
            final String ldapURL = r.toString();
            logger.debug("Fetching CRL from ldap {}", ldapURL);

            prepareConnectionFactory(ldapURL);
            connection = createConnection(ldapURL);

            logger.debug("Connected to {}. Searching {}", this.connectionConfig.getLdapUrl(), this.searchRequest);
            final SearchOperation searchOperation = new SearchOperation(connection);
            final Response<SearchResult> searchResult = searchOperation.execute(this.searchRequest);
            if (searchResult.getResultCode() == ResultCode.SUCCESS) {
                final LdapEntry entry = searchResult.getResult().getEntry();
                final LdapAttribute aval = entry.getAttribute(this.certificateRevocationListAttributeName);
                return fetchX509CRLFromAttribute(aval);
            }
            throw new CertificateException("Failed to establish a connection ldap. "
                        + searchResult.getMessage());

        } catch (final AuthenticationException | OperationNotSupportedException e) {
            logger.error(e.getMessage(), e);
            throw new CertificateException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
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
            final byte[] val = (byte[]) aval.getBinaryValue();
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
        throw new CertificateException("Attribute not found. Can not retrieve CRL from attribute: "
                + this.certificateRevocationListAttributeName);
    }

    /**
     * Create and open a connection to ldap
     * via the given config and provider.
     *
     * @param ldapURL the ldap uRL
     * @return the connection
     * @throws LdapException the ldap exception
     */
    protected Connection createConnection(final String ldapURL) throws LdapException {
        final Connection connection = this.connectionFactory.getConnection();
        connection.open();
        return connection;
    }

    /**
     * Prepare connection factory.
     *
     * @param ldapURL the ldap uRL
     */
    private void prepareConnectionFactory(final String ldapURL) {
        if (this.connectionFactory != null) {
            return;
        }

        if (StringUtils.isBlank(this.connectionConfig.getLdapUrl())) {
            logger.debug("Configuration does not indicate an LDAP url override. Setting ldap url to [{}]"
                    , ldapURL);
            this.connectionConfig.setLdapUrl(ldapURL);
        }
        logger.debug("Establishing a connection to {}", this.connectionConfig.getLdapUrl());

        if (this.connectionPool != null) {
            this.connectionPool.setConnectionFactory(
                    new DefaultConnectionFactory(this.connectionConfig, this.provider));
            this.connectionPool.initialize();
            this.connectionFactory = new PooledConnectionFactory(this.connectionPool);
            logger.debug("Connection pooling enabled. Using [{}]", this.connectionFactory.getClass().getName());
        } else {
            this.connectionFactory = new DefaultConnectionFactory(this.connectionConfig, this.provider);
            logger.debug("Connection pooling not configured. Using [{}]", this.connectionFactory.getClass().getName());
        }
    }
}
