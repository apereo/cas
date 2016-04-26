package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Credential;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Response;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link LdapAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back into
 * the LDAP instance.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("ldapAcceptableUsagePolicyRepository")
public class LdapAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    @Autowired
    @Qualifier("ldapUsagePolicyConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("ldapUsagePolicySearchRequest")
    private SearchRequest searchRequest;

    @Value("${cas.aup.ldap.search.filter:}")
    private String searchFilter;

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {

        try (final Connection searchConnection = getConnection()) {
            final Response<SearchResult> response = searchForId(searchConnection, credential.getId());
            if (hasResults(response)) {
                final String currentDn = response.getResult().getEntry().getDn();

                logger.debug("Updating {}", currentDn);

                try (final Connection modifyConnection = getConnection()) {
                    final ModifyOperation operation = new ModifyOperation(modifyConnection);
                    final List<AttributeModification> mods = new ArrayList<>();

                    final LdapEntry entry = new LdapEntry(currentDn, new LdapAttribute(this.aupAttributeName,
                            Boolean.TRUE.toString()));
                    for (final LdapAttribute attr : entry.getAttributes()) {
                        mods.add(new AttributeModification(AttributeModificationType.REPLACE, attr));
                    }
                    final ModifyRequest request = new ModifyRequest(currentDn,
                            mods.toArray(new AttributeModification[]{}));
                    operation.execute(request);
                } catch (final LdapException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Search for service by id.
     *
     * @param connection the connection
     * @param id         the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForId(final Connection connection, final String id)
            throws LdapException {

        final SearchFilter filter = new SearchFilter(this.searchFilter);
        filter.setParameter(0, id);
        return executeSearchOperation(connection, filter);
    }

    /**
     * Gets connection from the factory.
     * Opens the connection if needed.
     *
     * @return the connection
     * @throws LdapException the ldap exception
     */
    private Connection getConnection() throws LdapException {
        final Connection c = this.connectionFactory.getConnection();
        if (!c.isOpen()) {
            c.open();
        }
        return c;
    }

    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    private boolean hasResults(final Response<SearchResult> response) {
        final SearchResult result = response.getResult();
        if (result != null && result.getEntry() != null) {
            return true;
        }

        logger.trace("Requested ldap operation did not return a result or an ldap entry. Code: {}, Message: {}",
                response.getResultCode(), response.getMessage());
        return false;
    }


    /**
     * Execute search operation.
     *
     * @param connection the connection
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> executeSearchOperation(final Connection connection, final SearchFilter id)
            throws LdapException {

        final SearchOperation searchOperation = new SearchOperation(connection);
        final SearchRequest request = newSearchRequest(id);
        logger.debug("Using search request {}", request.toString());
        return searchOperation.execute(request);
    }

    /**
     * Builds a new request.
     *
     * @return the search request
     */
    private SearchRequest newSearchRequest(final SearchFilter filter) {
        final SearchRequest sr = new SearchRequest(this.searchRequest.getBaseDn(), filter);
        sr.setBinaryAttributes(ReturnAttributes.ALL_USER.value());
        sr.setDerefAliases(this.searchRequest.getDerefAliases());
        sr.setSearchEntryHandlers(this.searchRequest.getSearchEntryHandlers());
        sr.setSearchReferenceHandlers(this.searchRequest.getSearchReferenceHandlers());
        sr.setReferralHandler(this.searchRequest.getReferralHandler());
        sr.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        sr.setSearchScope(this.searchRequest.getSearchScope());
        sr.setSizeLimit(this.searchRequest.getSizeLimit());
        sr.setSortBehavior(this.searchRequest.getSortBehavior());
        sr.setTimeLimit(this.searchRequest.getTimeLimit());
        sr.setTypesOnly(this.searchRequest.getTypesOnly());
        sr.setControls(this.searchRequest.getControls());
        return sr;
    }

}
