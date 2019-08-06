package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchResult;
import org.springframework.webflow.execution.RequestContext;

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
}
