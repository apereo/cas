package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
@Slf4j
public class LdapAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    private final transient ConnectionFactory connectionFactory;
    private final AcceptableUsagePolicyProperties.Ldap ldapProperties;

    public LdapAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final String aupAttributeName,
                                               final ConnectionFactory connectionFactory,
                                               final AcceptableUsagePolicyProperties.Ldap ldapProperties) {
        super(ticketRegistrySupport, aupAttributeName);
        this.connectionFactory = connectionFactory;
        this.ldapProperties = ldapProperties;
    }
    
    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
    	val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        if (isUsagePolicyAcceptedBy(principal)) {
            LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
            return AcceptableUsagePolicyStatus.accepted(principal);
        }    	
        try {
        	    LOGGER.debug("AUP Attempt direct ldap call for [{}]", principal.getId());
                String[] returnAttributes = {aupAttributeName};
				Response<SearchResult> result = LdapUtils.executeSearchOperation(connectionFactory, ldapProperties.getBaseDn(), new SearchFilter("cn=" + credential.getId()), ldapProperties.getPageSize(),null,returnAttributes);
				for (Iterator<LdapEntry> iter = result.getResult().getEntries().iterator(); iter.hasNext(); ) {
				    LdapEntry element = iter.next();
				    if ( !(element.getAttribute() == null) && element.getAttribute().getName().equalsIgnoreCase(aupAttributeName)) {
				    	LOGGER.debug("Evaluating attribute value [{}] found for [{}]", element.getAttribute().getStringValue(), this.aupAttributeName);
		                if (element.getAttribute().getStringValue().toString().toUpperCase().equals("TRUE")) {
		                	LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
		                	return AcceptableUsagePolicyStatus.accepted(principal);
		                }
				    }
				}
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Usage policy has not been accepted by [{}]", principal.getId());        
        return AcceptableUsagePolicyStatus.denied(principal);
    }    

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            val response = searchForId(credential.getId());
            if (LdapUtils.containsResultEntry(response)) {
                val currentDn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating [{}]", currentDn);
                val attributes = CollectionUtils.<String, Set<String>>wrap(this.aupAttributeName,
                    CollectionUtils.wrapSet(Boolean.TRUE.toString().toUpperCase()));
                return LdapUtils.executeModifyOperation(currentDn, this.connectionFactory, attributes);
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
        val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            CollectionUtils.wrap(id));
        return LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize());
    }
    
    /**
     * Is usage policy accepted by user?
     * Looks into the attributes collected by the principal to find {@link #aupAttributeName}.
     * If the attribute contains {@code true}, then the policy is determined as accepted.
     *
     * @param principal the principal
     * @return true if accepted, false otherwise.
     */
    protected boolean isUsagePolicyAcceptedBy(final Principal principal) {
        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);

        if (attributes != null && attributes.containsKey(this.aupAttributeName)) {
            val value = CollectionUtils.toCollection(attributes.get(this.aupAttributeName));
            LOGGER.debug("Evaluating attribute value [{}] found for [{}]", value, this.aupAttributeName);
            return value.stream().anyMatch(v -> v.toString().equalsIgnoreCase(Boolean.TRUE.toString()));
        }
        return false;
    }    
}
