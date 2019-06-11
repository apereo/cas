package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties.Ldap;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link LdapConsentRepository}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Slf4j
public class LdapConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 8561763114482490L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final transient ConnectionFactory connectionFactory;
    private final Ldap ldap;
    private final String searchFilter;

    public LdapConsentRepository(final ConnectionFactory connectionFactory, final Ldap ldap) {
        this.connectionFactory = connectionFactory;
        this.ldap = ldap;
        this.searchFilter = '(' + this.ldap.getSearchFilter() + ')';
    }

    private static ConsentDecision mapFromJson(final String json) {
        try {
            LOGGER.trace("Mapping JSON value [{}] to consent object", json);
            return MAPPER.readValue(json, ConsentDecision.class);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String mapToJson(final ConsentDecision consent) {
        try {
            val json = MAPPER.writeValueAsString(consent);
            LOGGER.trace("Transformed consent object [{}] as JSON value [{}]", consent, json);
            return json;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        val principal = authentication.getPrincipal().getId();
        val entry = readConsentEntry(principal);
        if (entry != null) {
            val consentDecisions = entry.getAttribute(this.ldap.getConsentAttributeName());
            if (consentDecisions != null) {
                val values = consentDecisions.getStringValues();
                LOGGER.debug("Locating consent decision(s) for [{}] and service [{}]", principal, service.getId());
                return values
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .filter(d -> d.getService().equals(service.getId()))
                    .findFirst()
                    .orElse(null);
            }
        }
        return null;
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        val entry = readConsentEntry(principal);
        if (entry != null) {
            val consentDecisions = entry.getAttribute(this.ldap.getConsentAttributeName());
            if (consentDecisions != null) {
                LOGGER.debug("Located consent decision for [{}] at attribute [{}]", principal, this.ldap.getConsentAttributeName());
                return consentDecisions.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .collect(Collectors.toSet());
            }
        }
        return new HashSet<>(0);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        val entries = readConsentEntries();
        if (entries != null && !entries.isEmpty()) {
            val decisions = new HashSet<ConsentDecision>();
            entries
                .stream()
                .map(e -> e.getAttribute(this.ldap.getConsentAttributeName()))
                .filter(Objects::nonNull)
                .map(attr -> attr.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .collect(Collectors.toSet()))
                .forEach(decisions::addAll);
            return CollectionUtils.wrap(decisions);
        }
        LOGGER.debug("No consent decision could be found");
        return new HashSet<>(0);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        val entry = readConsentEntry(decision.getPrincipal());
        if (entry != null) {
            val newConsent = mergeDecision(entry.getAttribute(this.ldap.getConsentAttributeName()), decision);
            return executeModifyOperation(newConsent, entry);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) {
        LOGGER.debug("Deleting consent decision [{}] for principal [{}]", id, principal);
        val entry = readConsentEntry(principal);
        if (entry != null) {
            val newConsent = removeDecision(entry.getAttribute(this.ldap.getConsentAttributeName()), id);
            return executeModifyOperation(newConsent, entry);
        }
        return false;
    }

    /**
     * Modifies the consent decisions attribute on the entry.
     *
     * @param newConsent new set of consent decisions
     * @param entry      entry of consent decisions
     * @return true / false
     */
    private boolean executeModifyOperation(final Set<String> newConsent, final LdapEntry entry) {
        val attrMap = new HashMap<String, Set<String>>();
        attrMap.put(this.ldap.getConsentAttributeName(), newConsent);

        LOGGER.debug("Storing consent decisions [{}] at LDAP attribute [{}] for [{}]", newConsent, attrMap.keySet(), entry.getDn());
        return LdapUtils.executeModifyOperation(entry.getDn(), this.connectionFactory, CollectionUtils.wrap(attrMap));
    }

    /**
     * Merges a new decision into existing decisions.
     * Decisions are matched by ID.
     *
     * @param ldapConsent existing consent decisions
     * @param decision    new decision
     * @return new decision set
     */
    private static Set<String> mergeDecision(final LdapAttribute ldapConsent, final ConsentDecision decision) {
        if (decision.getId() < 0) {
            decision.setId(System.currentTimeMillis());
        }

        if (ldapConsent != null) {
            val result = removeDecision(ldapConsent, decision.getId());
            val json = mapToJson(decision);
            if (StringUtils.isBlank(json)) {
                throw new IllegalArgumentException("Could not map consent decision to JSON");
            }
            result.add(json);
            LOGGER.debug("Merged consent decision [{}] with LDAP attribute [{}]", decision, ldapConsent.getName());
            return CollectionUtils.wrap(result);
        }
        val result = new HashSet<String>();
        val json = mapToJson(decision);
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("Could not map consent decision to JSON");
        }
        result.add(json);
        return result;
    }

    /**
     * Removes decision from ldap attribute set.
     *
     * @param ldapConsent the ldap attribute holding consent decisions
     * @param decisionId  the decision Id
     * @return the new decision set
     */
    private static Set<String> removeDecision(final LdapAttribute ldapConsent, final long decisionId) {
        val result = new HashSet<String>();
        if (ldapConsent.size() != 0) {
            ldapConsent.getStringValues()
                .stream()
                .map(LdapConsentRepository::mapFromJson)
                .filter(Objects::nonNull)
                .filter(d -> d.getId() != decisionId)
                .map(LdapConsentRepository::mapToJson)
                .filter(Objects::nonNull)
                .forEach(result::add);
        }
        return result;
    }

    /**
     * Fetches a user entry along with its consent attributes.
     *
     * @param principal user name
     * @return the user's LDAP entry
     */
    private LdapEntry readConsentEntry(final String principal) {
        try {
            val filter = LdapUtils.newLdaptiveSearchFilter(this.searchFilter, CollectionUtils.wrapList(principal));
            LOGGER.debug("Locating consent LDAP entry via filter [{}] based on attribute [{}]", filter, this.ldap.getConsentAttributeName());
            val response =
                LdapUtils.executeSearchOperation(this.connectionFactory, this.ldap.getBaseDn(), filter, this.ldap.getPageSize(), this.ldap.getConsentAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getResult().getEntry();
                LOGGER.debug("Locating consent LDAP entry [{}]", entry);
                return entry;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Fetches all user entries that contain consent attributes along with these.
     *
     * @return the collection of user entries
     */
    private Collection<LdapEntry> readConsentEntries() {
        try {
            val att = this.ldap.getConsentAttributeName();
            val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");

            LOGGER.debug("Locating consent LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            val response = LdapUtils.executeSearchOperation(this.connectionFactory, this.ldap.getBaseDn(), filter, this.ldap.getPageSize(), att);
            if (LdapUtils.containsResultEntry(response)) {

                val results = response.getResult().getEntries();
                LOGGER.debug("Locating [{}] consent LDAP entries", results.size());
                return results;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }
}
