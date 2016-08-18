package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collections;

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
public class LdapAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;
    
    private ConnectionFactory connectionFactory;
    private String searchFilter;
    private String baseDn;

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {

        String currentDn = null;
        try {
            final Response<SearchResult> response = searchForId(credential.getId());
            if (LdapUtils.containsResultEntry(response)) {
                currentDn = response.getResult().getEntry().getDn();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (StringUtils.isNotBlank(currentDn)) {
            logger.debug("Updating {}", currentDn);
            return LdapUtils.executeModifyOperation(currentDn, this.connectionFactory,
                    Collections.singletonMap(this.aupAttributeName, 
                            Collections.singleton(Boolean.TRUE.toString())));
        }
        return false;
    }

    /**
     * Search for service by id.
     *
     * @param id         the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForId(final String id)
            throws LdapException {

        final SearchFilter filter = Beans.newSearchFilter(this.searchFilter, id);
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }
}
