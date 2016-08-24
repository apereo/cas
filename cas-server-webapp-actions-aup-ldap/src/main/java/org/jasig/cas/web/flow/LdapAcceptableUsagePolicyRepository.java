package org.jasig.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.util.LdapUtils;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
@Component("ldapAcceptableUsagePolicyRepository")
public class LdapAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    @Autowired
    @Qualifier("ldapUsagePolicyConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Value("${cas.aup.ldap.search.filter:}")
    private String searchFilter;

    @Value("${cas.aup.ldap.basedn:}")
    private String baseDn;

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {

        String currentDn = null;
        try (Connection searchConnection = LdapUtils.createConnection(this.connectionFactory)) {
            final Response<SearchResult> response = searchForId(searchConnection, credential.getId());
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
     * @param connection the connection
     * @param id         the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForId(final Connection connection, final String id)
            throws LdapException {

        final SearchFilter filter = new SearchFilter(this.searchFilter);
        filter.setParameter(0, id);
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }
}
