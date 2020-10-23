package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.LdapGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
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

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .setDefaultPrettyPrinter(new MinimalPrettyPrinter())
        .findAndRegisterModules();

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

    @Override
    public OneTimeTokenAccount get(final String username) {
        val entry = locateLdapEntryFor(username);
        if (entry != null) {
            val accounts = entry.getAttribute(ldapProperties.getAccountAttributeName());
            if (accounts != null) {
                LOGGER.debug("Located accounts for [{}] at attribute [{}]", username,
                    ldapProperties.getAccountAttributeName());
                val results = accounts.getStringValues()
                    .stream()
                    .map(LdapGoogleAuthenticatorTokenCredentialRepository::mapFromJson)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(this::decode)
                    .collect(Collectors.toList());
                if (!results.isEmpty()) {
                    val account = results.get(0);
                    LOGGER.debug("Located account [{}]", account);
                    return account;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val entries = locateLdapEntriesForAll();
        if (!entries.isEmpty()) {
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
        LOGGER.debug("No decision could be found");
        return new HashSet<>(0);
    }

    @Override
    public void save(final String username, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        update(new GoogleAuthenticatorAccount(username, secretKey, validationCode, scratchCodes));
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        if (account.getId() < 0) {
            account.setId(RandomUtils.nextLong());
        }
        LOGGER.debug("Storing account [{}]", account);
        val entry = locateLdapEntryFor(account.getUsername());
        val ldapAttribute = Objects.requireNonNull(entry,
            String.format("Unable to locate LDAP entry for %s", account.getUsername()))
            .getAttribute(ldapProperties.getAccountAttributeName());

        if (ldapAttribute == null || ldapAttribute.getStringValues().isEmpty()) {
            LOGGER.debug("Adding new account for LDAP entry [{}]", entry);
            val json = mapToJson(CollectionUtils.wrapArrayList(encode(account)));
            val accounts = new LinkedHashSet<String>();
            accounts.add(json);
            executeModifyOperation(accounts, entry);
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
    public long count() {
        return locateLdapEntriesForAll().size();
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    private boolean executeModifyOperation(final Set<String> accounts, final LdapEntry entry) {
        val attrMap = new HashMap<String, Set<String>>();
        attrMap.put(ldapProperties.getAccountAttributeName(), accounts);
        LOGGER.debug("Storing decisions [{}] at LDAP attribute [{}] for [{}]", accounts, attrMap.keySet(), entry.getDn());
        return LdapUtils.executeModifyOperation(entry.getDn(), connectionFactory, CollectionUtils.wrap(attrMap));
    }

    private Collection<LdapEntry> locateLdapEntriesForAll() {
        val att = ldapProperties.getAccountAttributeName();
        val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");
        try {
            LOGGER.debug("Locating LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            val response = LdapUtils.executeSearchOperation(connectionFactory,
                ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize(), att);
            if (LdapUtils.containsResultEntry(response)) {
                val results = response.getEntries();
                LOGGER.debug("Locating [{}] LDAP entries based on response [{}]", results.size(), response);
                return results;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        LOGGER.debug("Unable to read entries from LDAP via filter [{}]", filter);
        return new HashSet<>(0);
    }

    private LdapEntry locateLdapEntryFor(final String principal) {
        try {
            val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
            val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(principal));
            LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
                ldapProperties.getAccountAttributeName());
            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntry();
                LOGGER.debug("Locating LDAP entry [{}]", entry);
                return entry;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    private static String mapToJson(final Collection<OneTimeTokenAccount> acct) {
        try {
            val json = MAPPER.writeValueAsString(acct);
            LOGGER.trace("Transformed object [{}] as JSON value [{}]", acct, json);
            return json;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }


    private static List<OneTimeTokenAccount> mapFromJson(final String payload) {
        try {
            LOGGER.trace("Mapping JSON value [{}]", payload);
            val json = payload.trim();
            if (StringUtils.isNotBlank(json)) {
                return MAPPER.readValue(json, new TypeReference<>() {
                });
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }
}
