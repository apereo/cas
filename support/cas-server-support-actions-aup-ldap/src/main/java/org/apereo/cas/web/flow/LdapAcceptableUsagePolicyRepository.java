package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import java.util.Arrays;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAcceptableUsagePolicyRepository.class);
    
    private static final long serialVersionUID = 1600024683199961892L;

    private ConnectionFactory connectionFactory;
    private String searchFilter;
    private String baseDn;

    public LdapAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport, 
            final ConnectionFactory connectionFactory, final String searchFilter, final String baseDn) {
        super(ticketRegistrySupport);
        this.connectionFactory = connectionFactory;
        this.searchFilter = searchFilter;
        this.baseDn = baseDn;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        String currentDn = null;
        try {
            final Response<SearchResult> response = searchForId(credential.getId());
            if (LdapUtils.containsResultEntry(response)) {
                currentDn = response.getResult().getEntry().getDn();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (StringUtils.isNotBlank(currentDn)) {
            LOGGER.debug("Updating [{}]", currentDn);
            return LdapUtils.executeModifyOperation(currentDn, this.connectionFactory,
                    Collections.singletonMap(this.aupAttributeName,
                            Collections.singleton(Boolean.TRUE.toString())));
        }
        return false;
    }

    /**
     * Search for service by id.
     *
     * @param id the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForId(final String id) throws LdapException {

        final SearchFilter filter = Beans.newLdaptiveSearchFilter(this.searchFilter,
                Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                Arrays.asList(id));
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }
}
