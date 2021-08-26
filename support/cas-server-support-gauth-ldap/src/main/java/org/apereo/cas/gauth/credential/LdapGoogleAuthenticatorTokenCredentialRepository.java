package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.LdapGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link LdapGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Slf4j
public class LdapGoogleAuthenticatorTokenCredentialRepository
    extends BaseGoogleAuthenticatorTokenCredentialRepository
    implements DisposableBean {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final ConnectionFactory connectionFactory;

    private final LdapGoogleAuthenticatorMultifactorProperties ldapProperties;

    public LdapGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                            final IGoogleAuthenticator googleAuthenticator,
                                                            final ConnectionFactory connectionFactory,
                                                            final LdapGoogleAuthenticatorMultifactorProperties ldapProperties) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.connectionFactory = connectionFactory;
        this.ldapProperties = ldapProperties;
    }

    @SneakyThrows
    private static String mapToJson(final Collection<OneTimeTokenAccount> acct) {
        val json = MAPPER.writeValueAsString(acct);
        LOGGER.trace("Transformed object [{}] as JSON value [{}]", acct, json);
        return json;
    }

    @SneakyThrows
    private static List<OneTimeTokenAccount> mapFromJson(final String payload) {
        LOGGER.trace("Mapping JSON value [{}]", payload);
        val json = payload.trim();
        if (StringUtils.isNotBlank(json)) {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        }
        return new ArrayList<>(0);
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return load().stream().filter(acct -> acct.getId() == id).findFirst().orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return get(username).stream().filter(acct -> acct.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val entry = locateLdapEntryFor(username);
        if (entry != null) {
            val accounts = entry.getAttribute(ldapProperties.getAccountAttributeName());
            if (accounts != null) {
                LOGGER.debug("Located accounts for [{}] at attribute [{}]", username,
                    ldapProperties.getAccountAttributeName());
                return accounts.getStringValues()
                    .stream()
                    .map(LdapGoogleAuthenticatorTokenCredentialRepository::mapFromJson)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(this::decode)
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val entries = locateLdapEntriesForAll();
        if (!entries.isEmpty()) {
            return mapAccountsFromLdapEntries(entries);
        }
        LOGGER.debug("No decision could be found");
        return new HashSet<>(0);
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        if (account.getId() < 0) {
            account.setId(RandomUtils.nextLong());
        }
        LOGGER.debug("Storing account [{}]", account);
        val entry = locateLdapEntryFor(account.getUsername());
        val ldapAttribute = Objects.requireNonNull(entry,
            () -> String.format("Unable to locate LDAP entry for %s", account.getUsername()))
            .getAttribute(ldapProperties.getAccountAttributeName());

        if (ldapAttribute == null || ldapAttribute.getStringValues().isEmpty()) {
            LOGGER.debug("Adding new account for LDAP entry [{}]", entry);
            updateAccount(account, entry);
        } else {
            val existingAccounts = ldapAttribute.getStringValues()
                .stream()
                .map(LdapGoogleAuthenticatorTokenCredentialRepository::mapFromJson)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(this::decode)
                .collect(Collectors.toSet());
            val matchingAccount = existingAccounts.stream()
                .filter(acct -> acct.getId() == account.getId())
                .findFirst();
            matchingAccount.ifPresentOrElse(ac -> {
                ac.setValidationCode(account.getValidationCode());
                ac.setScratchCodes(account.getScratchCodes());
                ac.setSecretKey(account.getSecretKey());
            }, () -> existingAccounts.add(account));

            val accountsToSave = existingAccounts.stream()
                .map(acct -> encode(account))
                .filter(Objects::nonNull)
                .map(acct -> mapToJson(CollectionUtils.wrapArrayList(acct)))
                .collect(Collectors.toSet());
            executeModifyOperation(accountsToSave, entry);
        }
        return account;
    }

    @Override
    public void deleteAll() {
        val entries = locateLdapEntriesForAll();
        entries.forEach(entry -> executeModifyOperation(Set.of(), entry));
    }

    @Override
    public void delete(final String username) {
        LOGGER.debug("Deleting accounts for principal [{}]", username);
        val entry = locateLdapEntryFor(username);
        if (entry != null && executeModifyOperation(Set.of(), entry)) {
            LOGGER.debug("Successfully deleted accounts for [{}]", username);
        }
    }

    @Override
    public void delete(final long id) {
        val entry = searchLdapAccountsBy(id);
        if (entry != null) {
            val accounts = mapAccountsFromLdapEntries(List.of(entry));
            accounts.removeIf(device -> device.getId() == id);
            updateAccounts(accounts, entry);
        }
    }

    @Override
    public long count() {
        return locateLdapEntriesForAll().size();
    }

    @Override
    public long count(final String username) {
        return get(username).size();
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    private void updateAccount(final OneTimeTokenAccount account, final LdapEntry entry) {
        updateAccounts(List.of(account), entry);
    }

    private void updateAccounts(final Collection<OneTimeTokenAccount> accounts, final LdapEntry entry) {
        val results = accounts.stream().map(this::encode).collect(Collectors.toList());
        val json = mapToJson(results);
        val entries = new LinkedHashSet<String>();
        entries.add(json);
        executeModifyOperation(entries, entry);
    }

    private List<OneTimeTokenAccount> mapAccountsFromLdapEntries(final Collection<LdapEntry> entries) {
        return entries
            .stream()
            .map(e -> e.getAttribute(ldapProperties.getAccountAttributeName()))
            .filter(Objects::nonNull)
            .map(attr -> attr.getStringValues()
                .stream()
                .map(LdapGoogleAuthenticatorTokenCredentialRepository::mapFromJson)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(this::decode)
                .collect(Collectors.toSet()))
            .flatMap(Set::stream)
            .collect(Collectors.toList());
    }

    private boolean executeModifyOperation(final Set<String> accounts, final LdapEntry entry) {
        val attrMap = new HashMap<String, Set<String>>();
        attrMap.put(ldapProperties.getAccountAttributeName(), accounts);
        LOGGER.debug("Storing records [{}] at LDAP attribute [{}] for [{}]", accounts, attrMap.keySet(), entry.getDn());
        return LdapUtils.executeModifyOperation(entry.getDn(), connectionFactory, CollectionUtils.wrap(attrMap));
    }

    @SneakyThrows
    private Collection<LdapEntry> locateLdapEntriesForAll() {
        val att = ldapProperties.getAccountAttributeName();
        val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");
        LOGGER.debug("Locating LDAP entries via filter [{}] based on attribute [{}]", filter, att);
        val response = LdapUtils.executeSearchOperation(connectionFactory,
            ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize(), att);
        if (LdapUtils.containsResultEntry(response)) {
            val results = response.getEntries();
            LOGGER.debug("Locating [{}] LDAP entries based on response [{}]", results.size(), response);
            return results;
        }
        LOGGER.debug("Unable to read entries from LDAP via filter [{}]", filter);
        return new HashSet<>(0);
    }

    @SneakyThrows
    private LdapEntry locateLdapEntryFor(final String principal) {
        val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
        val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(principal));
        LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
            ldapProperties.getAccountAttributeName());
        val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
            filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
        if (LdapUtils.containsResultEntry(response)) {
            val entry = response.getEntry();
            LOGGER.debug("Located LDAP entry [{}]", entry);
            return entry;
        }
        return null;
    }

    @SneakyThrows
    private LdapEntry searchLdapAccountsBy(final long id) {
        val searchFilter = String.format("(%s=*\"id\":%s*)", ldapProperties.getAccountAttributeName(), id);
        val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter);
        LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
            ldapProperties.getAccountAttributeName());
        val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
            filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
        if (LdapUtils.containsResultEntry(response)) {
            val entry = response.getEntry();
            LOGGER.debug("Located LDAP entry [{}]", entry);
            return entry;
        }
        return null;
    }
}
