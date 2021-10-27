package org.jasig.cas.support.spnego.web.flow.client;

import org.apache.commons.lang3.StringUtils;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Peek into an LDAP server and check for the existence of an attribute
 * in order to target invocation of spnego.
 * @author Misagh Moayyed
 * @author Sean Baker
 * @since 4.1
 */
@Component("ldapSpnegoClientAction")
public class LdapSpnegoKnownClientSystemsFilterAction extends BaseSpnegoKnownClientSystemsFilterAction {
    /** Attribute name in LDAP to indicate spnego invocation. **/
    public static final String DEFAULT_SPNEGO_ATTRIBUTE = "distinguishedName";

    /** The must-have attribute name.*/
    @Value("${cas.spnego.ldap.attribute:spnegoAttribute}")
    protected String spnegoAttributeName;

    /** The connection configuration to ldap.*/
    @Autowired(required=false)
    @Qualifier("spnegoClientActionConnectionFactory")
    protected ConnectionFactory connectionFactory;

    /** The search request. */
    @Autowired(required=false)
    @Qualifier("spnegoClientActionSearchRequest")
    protected SearchRequest searchRequest;

    private LdapSpnegoKnownClientSystemsFilterAction() {}

    /**
     * Instantiates new action. Initializes the default attribute
     * to be {@link #DEFAULT_SPNEGO_ATTRIBUTE}.
     * @param connectionFactory the connection factory
     * @param searchRequest the search request
     */
    public LdapSpnegoKnownClientSystemsFilterAction(@NotNull final ConnectionFactory connectionFactory,
                                                    @NotNull final SearchRequest searchRequest) {
        this(connectionFactory, searchRequest, DEFAULT_SPNEGO_ATTRIBUTE);
    }

    /**
     * Instantiates a new action.
     *
     * @param connectionFactory the connection factory
     * @param searchRequest the search request
     * @param spnegoAttributeName the certificate revocation list attribute name
     */
    public LdapSpnegoKnownClientSystemsFilterAction(
            @NotNull final ConnectionFactory connectionFactory,
            @NotNull final SearchRequest searchRequest,
            @NotNull final String spnegoAttributeName) {
        this.connectionFactory = connectionFactory;
        this.spnegoAttributeName = spnegoAttributeName;
        this.searchRequest = searchRequest;
    }

    /**
     * Create and open a connection to ldap
     * via the given config and provider.
     *
     * @return the connection
     * @throws LdapException the ldap exception
     */
    protected Connection createConnection() throws LdapException {
        logger.debug("Establishing a connection...");
        final Connection connection = this.connectionFactory.getConnection();
        connection.open();
        return connection;
    }

    @Override
    protected boolean shouldDoSpnego(final String remoteIp) {

        if (StringUtils.isBlank(this.spnegoAttributeName)) {
            logger.warn("Ignoring Spnego. Attribute name is not configured");
            return false;
        }

        if (this.connectionFactory == null) {
            logger.warn("Ignoring Spnego. LDAP connection factory is not configured");
            return false;
        }

        if (this.searchRequest == null) {
            logger.warn("Ignoring Spnego. LDAP search request is not configured");
            return false;
        }

        final boolean ipCheck = ipPatternCanBeChecked(remoteIp);
        if (ipCheck && !ipPatternMatches(remoteIp)) {
            return false;
        }

        return executeSearchForSpnegoAttribute(remoteIp);
    }

    /**
     * Searches the ldap instance for the attribute value.
     *
     * @param remoteIp the remote ip
     * @return the boolean
     */
    protected boolean executeSearchForSpnegoAttribute(final String remoteIp) {
        Connection connection = null;
        final String remoteHostName = getRemoteHostName(remoteIp);
        logger.debug("Resolved remote hostname {} based on ip {}",
                remoteHostName, remoteIp);

        try {
            connection = createConnection();
            final Operation searchOperation = new SearchOperation(connection);
            this.searchRequest.getSearchFilter().setParameter(0, remoteHostName);

            logger.debug("Using search filter {} on baseDn {}",
                    this.searchRequest.getSearchFilter().format(),
                    this.searchRequest.getBaseDn());

            final Response<SearchResult> searchResult = searchOperation.execute(this.searchRequest);
            if (searchResult.getResultCode() == ResultCode.SUCCESS) {
                return processSpnegoAttribute(searchResult);
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
     *
     * @param searchResult the search result
     * @return true if attribute value exists and has a value
     */
    protected boolean processSpnegoAttribute(final Response<SearchResult> searchResult) {
        final SearchResult result = searchResult.getResult();

        if (result == null || result.getEntries().isEmpty()) {
            logger.debug("Spnego attribute is not found in the search results");
            return false;
        }
        final LdapEntry entry = result.getEntry();
        final LdapAttribute attribute = entry.getAttribute(this.spnegoAttributeName);
        return verifySpnegyAttributeValue(attribute);
    }

    /**
     * Verify spnegy attribute value.
     * This impl simply makes sure the attribute exists and has a value.
     * @param attribute the ldap attribute
     * @return true if available. false otherwise.
     */
    protected boolean verifySpnegyAttributeValue(final LdapAttribute attribute) {
        return attribute != null && StringUtils.isNotBlank(attribute.getStringValue());
    }
}
