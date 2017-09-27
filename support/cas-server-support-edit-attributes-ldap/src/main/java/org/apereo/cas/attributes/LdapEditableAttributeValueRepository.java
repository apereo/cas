package org.apereo.cas.attributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link LdapEditableAttributeValueRepository}.
 * Stores editable attributes inside an LDAP directory.
 *
 * @author Misagh Moayyed
 * @since 5.2
 */
public class LdapEditableAttributeValueRepository extends AbstractPrincipalEditableAttributeValueRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapEditableAttributeValueRepository.class);

    private static final long serialVersionUID = 56666958547848172L;

    private final ConnectionFactory connectionFactory;
    private final String searchFilter;
    private final String baseDn;

    public LdapEditableAttributeValueRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final ConnectionFactory connectionFactory, 
                                               final String searchFilter, final String baseDn) {
        super(ticketRegistrySupport);
        this.connectionFactory = connectionFactory;
        this.searchFilter = searchFilter;
        this.baseDn = baseDn;
    }

    @Override
	public boolean storeAttributeValues(RequestContext requestContext, Credential credential,
			Map<String, String> attributeValues) {
        
    	HashMap<String,Set<String>> ldapAttrs = new HashMap<>();
        attributeValues.forEach((k,v)-> {
        	ldapAttrs.put(k, Collections.singleton(v));
        });
    	
    	try {
            final Response<SearchResult> response = searchForId(credential.getId());
            if (LdapUtils.containsResultEntry(response)) {
                final String currentDn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating [{}]", currentDn);
                return LdapUtils.executeModifyOperation(currentDn, this.connectionFactory,ldapAttrs);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(this.searchFilter,
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(id));
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }
}
