package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.aup.LdapAcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.execution.RequestContext;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
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
public class LdapAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository implements DisposableBean {
    private static final long serialVersionUID = 1600024683199961892L;

    private final Map<String, ConnectionFactory> connectionFactoryList;

    public LdapAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final AcceptableUsagePolicyProperties aupProperties,
                                               final Map<String, ConnectionFactory> connectionFactoryList) {
        super(ticketRegistrySupport, aupProperties);
        this.connectionFactoryList = connectionFactoryList;
    }

    /**
     * Search ldap for id and return optional.
     *
     * @param ldap the ldap
     * @param id   the id
     * @return the optional
     * @throws Exception the exception
     */
    protected Optional<Pair<ConnectionFactory, SearchResponse>> searchLdapForId(final LdapAcceptableUsagePolicyProperties ldap,
                                                                                final String id) throws Exception {
        val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            CollectionUtils.wrap(id));
        LOGGER.debug("Constructed LDAP filter [{}]", filter);
        val connectionFactory = connectionFactoryList.get(ldap.getLdapUrl());
        val response = LdapUtils.executeSearchOperation(connectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
        if (LdapUtils.containsResultEntry(response)) {
            LOGGER.debug("LDAP query located an entry for [{}] and responded with [{}]", id, response);
            return Optional.of(Pair.of(connectionFactory, response));
        }
        LOGGER.debug("LDAP query could not locate an entry for [{}]", id);
        return Optional.empty();
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            val response = aupProperties.getLdap()
                .stream()
                .sorted(Comparator.comparing(LdapAcceptableUsagePolicyProperties::getName))
                .map(Unchecked.function(ldap -> searchLdapForId(ldap, credential.getId())))
                .filter(Optional::isPresent)
                .findFirst();

            if (response.isPresent()) {
                val result = response.get().get();
                val currentDn = result.getValue().getEntry().getDn();
                LOGGER.debug("Updating [{}]", currentDn);
                val attributes = CollectionUtils.<String, Set<String>>wrap(aupProperties.getAupAttributeName(),
                    CollectionUtils.wrapSet(Boolean.TRUE.toString().toUpperCase()));
                return LdapUtils.executeModifyOperation(currentDn, result.getKey(), attributes);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void destroy() {
        connectionFactoryList.forEach((ldap, connectionFactory) ->
            connectionFactory.close()
        );
    }
}
