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

package org.jasig.cas.support.spnego.web.flow.client;

import org.apache.commons.lang3.StringUtils;
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
import org.ldaptive.provider.Provider;

import javax.validation.constraints.NotNull;


/**
 * Peek into an LDAP server and check for the existence of an attribute
 * in order to target invocation of spnego.
 * @author Misagh Moayyed
 * @author Sean Baker
 * @since 4.1
 */
public class LdapSpnegoKnownClientSystemsFilterAction extends BaseSpnegoKnownClientSystemsFilterAction {

    /** Attribute name in LDAP to indicate spnego invocation. **/
    public static final String DEFAULT_SPNEGO_ATTRIBUTE = "distinguishedName";

    /** The must-have attribute name.*/
    protected final String spnegoAttributeName;

    /** The connection configuration to ldap.*/
    protected final ConnectionConfig connectionConfig;

    /** Search request to find the attribute in the ldap tree.*/
    protected final SearchRequest searchRequest;

    /** The ldap provider, defaults to
     * {@link DefaultConnectionFactory#getProvider()}.
     **/
    protected Provider provider = DefaultConnectionFactory.getDefaultProvider();

    /**
     * Instantiates new action. Initializes the default attribute
     * to be {@link #DEFAULT_SPNEGO_ATTRIBUTE}.
     * @param searchRequest the search request
     * @param connectionConfig the connection config
     */
    public LdapSpnegoKnownClientSystemsFilterAction(
            @NotNull final SearchRequest searchRequest,
            @NotNull final ConnectionConfig connectionConfig) {
        this(searchRequest, connectionConfig,
                DEFAULT_SPNEGO_ATTRIBUTE);
    }

    /**
     * Instantiates a new action.
     *
     * @param searchRequest the search request
     * @param connectionConfig the connection config
     * @param spnegoAttributeName the certificate revocation list attribute name
     */
    public LdapSpnegoKnownClientSystemsFilterAction(
            @NotNull final SearchRequest searchRequest,
            @NotNull final ConnectionConfig connectionConfig,
            @NotNull final String spnegoAttributeName) {
        super();
        this.spnegoAttributeName = spnegoAttributeName;
        this.connectionConfig = connectionConfig;
        this.searchRequest = searchRequest;
    }

    public final void setProvider(final Provider provider) {
        this.provider = provider;
    }

    /**
     * Create and open a connection to ldap
     * via the given config and provider.
     *
     * @return the connection
     * @throws LdapException the ldap exception
     */
    protected Connection createConnection() throws LdapException {
        logger.debug("Establishing a connection to {}", this.connectionConfig.getLdapUrl());
        final ConnectionFactory factory = new DefaultConnectionFactory(this.connectionConfig, this.provider);
        final Connection connection = factory.getConnection();
        connection.open();
        return connection;
    }

    @Override
    protected boolean shouldDoSpnego() {
        Connection connection = null;
        final String remoteHostName = this.getRemoteHostName ();
        try {
            connection = createConnection();
            logger.debug("Connected to {}. Searching {}", this.connectionConfig.getLdapUrl(), this.searchRequest);
            final SearchOperation searchOperation = new SearchOperation(connection);
            this.searchRequest.getSearchFilter().setParameter(0, remoteHostName);
            final Response<SearchResult> searchResult = searchOperation.execute(this.searchRequest);
            if (searchResult.getResultCode() == ResultCode.SUCCESS) {
                return verifySpnegoAttribute(searchResult);
            }
            throw new RuntimeException("Failed to establish a connection ldap. "
                    + searchResult.getMessage());

        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Verify spnego attribute value.
     * This impl simply makes sure the attribute exists and has a value.
     *
     * @param searchResult the search result
     * @return true if attribute value exists and has a value
     */
    protected boolean verifySpnegoAttribute(final Response<SearchResult> searchResult) {
    	if (searchResult.getResult() == null || searchResult.getResult().getEntries().size() < 1) {
    		return false;
    	}
        final LdapEntry entry = searchResult.getResult().getEntry();
        final LdapAttribute attribute = entry.getAttribute(this.spnegoAttributeName);
        return attribute != null && StringUtils.isNotBlank(attribute.getStringValue());
    }
}
