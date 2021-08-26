package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.LdapConsentProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link LdapConsentRepository}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapConsentRepository implements ConsentRepository, DisposableBean {
    private static final long serialVersionUID = 8561763114482490L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ConnectionFactory connectionFactory;

    private final LdapConsentProperties ldapProperties;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        val principal = authentication.getPrincipal().getId();
        val entry = readConsentEntry(principal);
        if (entry != null) {
            val consentDecisions = entry.getAttribute(ldapProperties.getConsentAttributeName());
            if (consentDecisions != null) {
                val values = consentDecisions.getStringValues();
                LOGGER.debug("Locating consent decision(s) for [{}] and service [{}]", principal, service.getId());
                return values
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .filter(Objects::nonNull)
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
            val consentDecisions = entry.getAttribute(ldapProperties.getConsentAttributeName());
            if (consentDecisions != null) {
                LOGGER.debug("Located consent decision for [{}] at attribute [{}]", principal, ldapProperties.getConsentAttributeName());
                return consentDecisions.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            }
        }
        return new HashSet<>(0);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        val entries = readConsentEntries();
        if (!entries.isEmpty()) {
            return entries
                .stream()
                .map(e -> e.getAttribute(this.ldapProperties.getConsentAttributeName()))
                .filter(Objects::nonNull)
                .map(attr -> attr.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toList());
        }
        LOGGER.debug("No consent decision could be found");
        return new HashSet<>(0);
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        LOGGER.debug("Storing consent decision [{}]", decision);
        val entry = readConsentEntry(decision.getPrincipal());
        if (entry != null) {
            val newConsent = mergeDecision(entry.getAttribute(ldapProperties.getConsentAttributeName()), decision);
            if (executeModifyOperation(newConsent, entry)) {
                return decision;
            }
        }
        LOGGER.debug("Unable to read consent entry for [{}]. Consent decision is not stored", decision.getPrincipal());
        return null;
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) {
        LOGGER.debug("Deleting consent decision [{}] for principal [{}]", id, principal);
        val entry = readConsentEntry(principal);
        if (entry != null) {
            val newConsent = removeDecision(entry.getAttribute(this.ldapProperties.getConsentAttributeName()), id);
            return executeModifyOperation(newConsent, entry);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        LOGGER.debug("Deleting consent decisions for principal [{}]", principal);
        val entry = readConsentEntry(principal);
        return entry != null && executeModifyOperation(new HashSet<>(), entry);
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    private static ConsentDecision mapFromJson(final String json) {
        try {
            LOGGER.trace("Mapping JSON value [{}] to consent object", json);
            return MAPPER.readValue(JsonValue.readHjson(json).toString(), ConsentDecision.class);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @SneakyThrows
    private static String mapToJson(final ConsentDecision consent) {
        val json = MAPPER.writeValueAsString(consent);
        LOGGER.trace("Transformed consent object [{}] as JSON value [{}]", consent, json);
        return json;
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
            result.add(json);
            LOGGER.debug("Merged consent decision [{}] with LDAP attribute [{}]", decision, ldapConsent.getName());
            return CollectionUtils.wrap(result);
        }
        val json = mapToJson(decision);
        val result = new HashSet<String>(1);
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
        return removeDecisions(ldapConsent, d -> d.getId() != decisionId);
    }

    private static Set<String> removeDecisions(final LdapAttribute ldapConsent, final Predicate<ConsentDecision> filter) {
        if (ldapConsent.size() != 0) {
            return ldapConsent.getStringValues()
                .stream()
                .map(LdapConsentRepository::mapFromJson)
                .filter(Objects::nonNull)
                .filter(filter)
                .map(LdapConsentRepository::mapToJson)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        return new HashSet<>(0);
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
        attrMap.put(this.ldapProperties.getConsentAttributeName(), newConsent);

        LOGGER.debug("Storing consent decisions [{}] at LDAP attribute [{}] for [{}]", newConsent, attrMap.keySet(), entry.getDn());
        return LdapUtils.executeModifyOperation(entry.getDn(), this.connectionFactory, CollectionUtils.wrap(attrMap));
    }

    /**
     * Fetches a user entry along with its consent attributes.
     *
     * @param principal user name
     * @return the user's LDAP entry
     */
    private LdapEntry readConsentEntry(final String principal) {
        try {
            val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
            val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(principal));
            LOGGER.debug("Locating consent LDAP entry via filter [{}] based on attribute [{}]", filter, ldapProperties.getConsentAttributeName());
            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), ldapProperties.getConsentAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntry();
                LOGGER.debug("Locating consent LDAP entry [{}]", entry);
                return entry;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Fetches all user entries that contain consent attributes along with these.
     *
     * @return the collection of user entries
     */
    private Collection<LdapEntry> readConsentEntries() {
        val att = ldapProperties.getConsentAttributeName();
        try {
            val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");
            LOGGER.debug("Locating consent LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), att);
            if (LdapUtils.containsResultEntry(response)) {
                val results = response.getEntries();
                LOGGER.debug("Locating [{}] consent LDAP entries based on response [{}]", results.size(), response);
                return results;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.debug("Unable to read consent entries from LDAP for attribute [{}]", att);
        return new HashSet<>(0);
    }
}
