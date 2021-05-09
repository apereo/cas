package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link LdapWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class LdapWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository implements DisposableBean {
    private final ConnectionFactory connectionFactory;

    public LdapWebAuthnCredentialRepository(final ConnectionFactory connectionFactory,
        final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val ldapProperties = getProperties().getAuthn().getMfa().getWebAuthn().getLdap();
        return Stream.ofNullable(locateLdapEntryFor(username))
            .filter(Objects::nonNull)
            .map(e -> e.getAttribute(ldapProperties.getAccountAttributeName()))
            .filter(Objects::nonNull)
            .map(attr -> attr.getStringValues()
                .stream()
                .filter(Objects::nonNull)
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .map(record -> getCipherExecutor().decode(record))
                .filter(Objects::nonNull)
                .map(LdapWebAuthnCredentialRepository::mapFromJson)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet()))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        val ldapProperties = getProperties().getAuthn().getMfa().getWebAuthn().getLdap();
        return locateLdapEntriesForAll()
            .map(e -> e.getAttribute(ldapProperties.getAccountAttributeName()))
            .filter(Objects::nonNull)
            .map(attr -> attr.getStringValues()
                .stream()
                .filter(Objects::nonNull)
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .map(record -> getCipherExecutor().decode(record))
                .filter(Objects::nonNull)
                .map(LdapWebAuthnCredentialRepository::mapFromJson)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet()))
            .flatMap(Set::stream);
    }

    @Override
    @SneakyThrows
    protected void update(final String username, final Collection<CredentialRegistration> givenRecords) {
        if (givenRecords.isEmpty()) {
            LOGGER.debug("No records are provided for [{}] so entry will be removed", username);
            executeModifyOperation(new HashSet<>(0), Optional.ofNullable(locateLdapEntryFor(username)));
        } else {
            val records = givenRecords.stream()
                .map(record -> {
                    if (record.getRegistrationTime() == null) {
                        return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                    }
                    return record;
                })
                .collect(Collectors.toList());
            val results = records.stream()
                .map(Unchecked.function(reg -> WebAuthnUtils.getObjectMapper().writeValueAsString(records)))
                .map(reg -> getCipherExecutor().encode(reg))
                .collect(Collectors.toSet());
            executeModifyOperation(results, Optional.ofNullable(locateLdapEntryFor(username)));
        }
    }

    private LdapEntry locateLdapEntryFor(final String principal) {
        try {
            val ldapProperties = getProperties().getAuthn().getMfa().getWebAuthn().getLdap();
            val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
            val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(principal.trim().toLowerCase()));
            LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
                ldapProperties.getAccountAttributeName());
            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntry();
                LOGGER.debug("Located LDAP entry [{}]", entry);
                return entry;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    private boolean executeModifyOperation(final Set<String> accounts, final Optional<LdapEntry> result) {
        if (result.isPresent()) {
            val entry = result.get();
            val ldapProperties = getProperties().getAuthn().getMfa().getWebAuthn().getLdap();
            val attrMap = new HashMap<String, Set<String>>();
            attrMap.put(ldapProperties.getAccountAttributeName(), accounts);
            LOGGER.debug("Storing records [{}] at LDAP attribute [{}] for [{}]", accounts, attrMap.keySet(), entry.getDn());
            return LdapUtils.executeModifyOperation(entry.getDn(), connectionFactory, CollectionUtils.wrap(attrMap));
        }
        return false;
    }

    private Stream<LdapEntry> locateLdapEntriesForAll() {
        val ldapProperties = getProperties().getAuthn().getMfa().getWebAuthn().getLdap();
        val att = ldapProperties.getAccountAttributeName();
        val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");
        try {
            LOGGER.debug("Locating LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            val response = LdapUtils.executeSearchOperation(connectionFactory,
                ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize(), att);
            if (LdapUtils.containsResultEntry(response)) {
                val results = response.getEntries();
                LOGGER.debug("Locating [{}] LDAP entries based on response [{}]", results.size(), response);
                return results.stream();
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        LOGGER.debug("Unable to read entries from LDAP via filter [{}]", filter);
        return Stream.empty();
    }

    private static List<CredentialRegistration> mapFromJson(final String payload) {
        try {
            LOGGER.trace("Mapping JSON value [{}]", payload);
            val json = payload.trim();
            if (StringUtils.isNotBlank(json)) {
                return WebAuthnUtils.getObjectMapper().readValue(json, new TypeReference<>() {
                });
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }
}
