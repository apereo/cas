package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.aup.LdapAcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
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

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
        var status = super.verify(requestContext, credential);
        if (!status.isAccepted()) {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            return aupProperties.getLdap()
                .stream()
                .sorted(Comparator.comparing(LdapAcceptableUsagePolicyProperties::getName))
                .map(Unchecked.function(ldap -> searchLdapForId(ldap, principal.getId())))
                .filter(Optional::isPresent)
                .findFirst()
                .filter(Optional::isPresent)
                .map(result -> result.get().getMiddle().getEntry())
                .map(entry -> {
                    val attribute = entry.getAttribute(aupProperties.getAupAttributeName());
                    return attribute != null && attribute.getStringValues()
                        .stream()
                        .anyMatch(value -> value.equalsIgnoreCase(Boolean.TRUE.toString()));
                })
                .map(result -> new AcceptableUsagePolicyStatus(result, status.getPrincipal()))
                .orElse(AcceptableUsagePolicyStatus.denied(status.getPrincipal()));
        }
        return status;
    }

    /**
     * Search ldap for id and return optional.
     *
     * @param ldap the ldap
     * @param id   the id
     * @return the optional
     * @throws Exception the exception
     */
    protected Optional<Triple<ConnectionFactory, SearchResponse, LdapAcceptableUsagePolicyProperties>>
        searchLdapForId(final LdapAcceptableUsagePolicyProperties ldap, final String id) throws Exception {

        val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            CollectionUtils.wrap(id));
        LOGGER.debug("Constructed LDAP filter [{}]", filter);
        val connectionFactory = connectionFactoryList.get(ldap.getLdapUrl());
        val response = LdapUtils.executeSearchOperation(connectionFactory,
            ldap.getBaseDn(), filter, ldap.getPageSize());
        if (LdapUtils.containsResultEntry(response)) {
            LOGGER.debug("LDAP query located an entry for [{}] and responded with [{}]", id, response);
            return Optional.of(Triple.of(connectionFactory, response, ldap));
        }
        LOGGER.debug("LDAP query could not locate an entry for [{}]", id);
        return Optional.empty();
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val response = aupProperties.getLdap()
            .stream()
            .sorted(Comparator.comparing(LdapAcceptableUsagePolicyProperties::getName))
            .map(Unchecked.function(ldap -> searchLdapForId(ldap, principal.getId())))
            .filter(Optional::isPresent)
            .findFirst();

        if (response.isPresent()) {
            val result = response.get().get();
            val currentDn = result.getMiddle().getEntry().getDn();
            LOGGER.debug("Updating [{}]", currentDn);
            val attributes = CollectionUtils.<String, Set<String>>wrap(aupProperties.getAupAttributeName(),
                CollectionUtils.wrapSet(Boolean.TRUE.toString().toUpperCase()));
            return LdapUtils.executeModifyOperation(currentDn, result.getLeft(), attributes);
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
