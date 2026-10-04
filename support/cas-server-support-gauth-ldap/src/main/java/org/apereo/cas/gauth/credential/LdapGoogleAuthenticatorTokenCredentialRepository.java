package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.LdapGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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

    private final LdapConnectionFactory connectionFactory;

    private final LdapGoogleAuthenticatorMultifactorProperties ldapProperties;

    public LdapGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                            final CipherExecutor<Number, Number> scratchCodesCipher,
                                                            final CasGoogleAuthenticator googleAuthenticator,
                                                            final ConnectionFactory connectionFactory,
                                                            final LdapGoogleAuthenticatorMultifactorProperties ldapProperties) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.connectionFactory = new LdapConnectionFactory(connectionFactory);
        this.ldapProperties = ldapProperties;
    }

    private static String mapToJson(final Collection<OneTimeTokenAccount> acct) {
        return FunctionUtils.doUnchecked(() -> {
            val json = MAPPER.writeValueAsString(acct);
            LOGGER.trace("Transformed object [{}] as JSON value [{}]", acct, json);
            return json;
        });
    }

    private static List<OneTimeTokenAccount> mapFromJson(final String payload) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.trace("Mapping JSON value [{}]", payload);
            val json = payload.trim();
            if (StringUtils.isNotBlank(json)) {
                return MAPPER.readValue(json, new TypeReference<>() {
                });
            }
            return new ArrayList<>();
        });
    }

    /**
     * Find the account by id. The entry that holds it is located with the id search rather than by reading every
     * entry, and only the matching account is decoded.
     *
     * @param id the account id
     * @return the decoded account, or null when there is none
     */
    @Override
    public @Nullable OneTimeTokenAccount get(final long id) {
        return Optional.ofNullable(searchLdapAccountsBy(id))
            .flatMap(entry -> findStoredAccount(entry, id))
            .map(this::decode)
            .orElse(null);
    }

    @Override
    public @Nullable OneTimeTokenAccount get(final String username, final long id) {
        return Optional.ofNullable(locateLdapEntryFor(username))
            .flatMap(entry -> findStoredAccount(entry, id))
            .map(this::decode)
            .orElse(null);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val entry = locateLdapEntryFor(username);
        if (entry != null) {
            LOGGER.debug("Located accounts for [{}] at attribute [{}]", username, ldapProperties.getAccountAttributeName());
            return readStoredAccounts(entry).stream().map(this::decode).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val entries = locateLdapEntriesForAll();
        if (!entries.isEmpty()) {
            return mapAccountsFromLdapEntries(entries);
        }
        LOGGER.debug("No decision could be found");
        return new HashSet<>();
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account.assignIdIfNecessary());
    }

    /**
     * Store the account in the user's entry, keeping the entry's other accounts. Only the given account is encoded;
     * the entry's other accounts are written back exactly as they were stored. The account replaces a stored account
     * with the same id, or is added. All of them are written back as a single JSON array value, because some
     * directories keep only one value of the account attribute; Active Directory, for example, treats
     * {@code description} as single-valued on user objects.
     *
     * @param account the account
     * @return the account
     */
    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        if (account.getId() < 0) {
            account.setId(RandomUtils.nextLong());
        }
        LOGGER.debug("Storing account [{}]", account);
        val entry = Objects.requireNonNull(locateLdapEntryFor(account.getUsername()),
            () -> String.format("Unable to locate LDAP entry for %s", account.getUsername()));
        val accounts = readStoredAccounts(entry);
        accounts.removeIf(stored -> stored.getId() == account.getId());
        accounts.add(encode(account));
        writeStoredAccounts(accounts, entry);
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

    /**
     * Remove the account from the entry that holds it; the entry's other accounts are written back as stored.
     *
     * @param id the account id
     */
    @Override
    public void delete(final long id) {
        val entry = searchLdapAccountsBy(id);
        if (entry != null) {
            val accounts = readStoredAccounts(entry);
            accounts.removeIf(device -> device.getId() == id);
            writeStoredAccounts(accounts, entry);
        }
    }

    /**
     * Count the stored accounts, that is the devices, across all entries; an entry can hold several.
     * Accounts are counted as stored, without being decoded.
     *
     * @return the number of accounts
     */
    @Override
    public long count() {
        return locateLdapEntriesForAll()
            .stream()
            .mapToLong(entry -> readStoredAccounts(entry).size())
            .sum();
    }

    @Override
    public long count(final String username) {
        return Optional.ofNullable(locateLdapEntryFor(username))
            .map(entry -> readStoredAccounts(entry).size())
            .orElse(0);
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    /**
     * Write the stored, already encoded, accounts back to the entry as a single JSON array value.
     *
     * @param accounts the encoded accounts
     * @param entry    the entry
     */
    private void writeStoredAccounts(final Collection<OneTimeTokenAccount> accounts, final LdapEntry entry) {
        val entries = new LinkedHashSet<String>();
        entries.add(mapToJson(accounts));
        executeModifyOperation(entries, entry);
    }

    /**
     * Read the accounts stored in the entry as they are stored, that is encoded, from every value of the account
     * attribute. The returned list can be changed and written back with {@link #writeStoredAccounts(Collection, LdapEntry)}.
     *
     * @param entry the entry
     * @return the encoded accounts
     */
    private List<OneTimeTokenAccount> readStoredAccounts(final LdapEntry entry) {
        return Optional.ofNullable(entry.getAttribute(ldapProperties.getAccountAttributeName()))
            .stream()
            .flatMap(attribute -> attribute.getStringValues().stream())
            .map(LdapGoogleAuthenticatorTokenCredentialRepository::mapFromJson)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private Optional<OneTimeTokenAccount> findStoredAccount(final LdapEntry entry, final long id) {
        return readStoredAccounts(entry).stream().filter(account -> account.getId() == id).findFirst();
    }

    private List<OneTimeTokenAccount> mapAccountsFromLdapEntries(final Collection<LdapEntry> entries) {
        return entries
            .stream()
            .map(entry -> readStoredAccounts(entry).stream().map(this::decode).collect(Collectors.toSet()))
            .flatMap(Set::stream)
            .collect(Collectors.toList());
    }

    private boolean executeModifyOperation(final Set<String> accounts, final LdapEntry entry) {
        val attrMap = new HashMap<String, Set<String>>();
        attrMap.put(ldapProperties.getAccountAttributeName(), accounts);
        LOGGER.debug("Storing records [{}] at LDAP attribute [{}] for [{}]", accounts, attrMap.keySet(), entry.getDn());
        return connectionFactory.executeModifyOperation(entry.getDn(), CollectionUtils.wrap(attrMap));
    }

    private Collection<LdapEntry> locateLdapEntriesForAll() {
        return FunctionUtils.doUnchecked(() -> {
            val att = ldapProperties.getAccountAttributeName();
            val filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");
            LOGGER.debug("Locating LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            val response = connectionFactory.executeSearchOperation(
                ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize(), att);
            if (LdapUtils.containsResultEntry(response)) {
                val results = response.getEntries();
                LOGGER.debug("Locating [{}] LDAP entries based on response [{}]", results.size(), response);
                return results;
            }
            LOGGER.debug("Unable to read entries from LDAP via filter [{}]", filter);
            return new HashSet<>();
        });
    }

    private @Nullable LdapEntry locateLdapEntryFor(final String principal) {
        return FunctionUtils.doUnchecked(() -> {
            val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
            val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(principal));
            LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
                ldapProperties.getAccountAttributeName());
            val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntry();
                LOGGER.debug("Located LDAP entry [{}]", entry);
                return entry;
            }
            return null;
        });
    }

    private @Nullable LdapEntry searchLdapAccountsBy(final long id) {
        return FunctionUtils.doUnchecked(() -> {
            val searchFilterWithoutSpaces = String.format("(%s=*\"id\":%s*)", ldapProperties.getAccountAttributeName(), id);
            val searchFilterWithSpaces = String.format("(%s=*\"id\" : %s*)", ldapProperties.getAccountAttributeName(), id);
            val filterQuery = "(|" + searchFilterWithoutSpaces + searchFilterWithSpaces + ')';
            val filter = LdapUtils.newLdaptiveSearchFilter(filterQuery);
            LOGGER.debug("Locating LDAP entry via filter [{}] based on attribute [{}]", filter,
                ldapProperties.getAccountAttributeName());
            val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize(), ldapProperties.getAccountAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntries()
                    .stream()
                    .filter(candidate -> containsAccount(candidate, id))
                    .findFirst()
                    .orElse(null);
                LOGGER.debug("Located LDAP entry [{}]", entry);
                return entry;
            }
            return null;
        });
    }

    /**
     * Whether the entry stores an account with the given id. The search filter can only match the id as a substring
     * of the stored JSON, so it also matches entries whose accounts have longer ids that start with the same digits;
     * this check keeps only the entry that really holds the account.
     *
     * @param entry the LDAP entry
     * @param id    the account id
     * @return true if the entry stores the account
     */
    private boolean containsAccount(final LdapEntry entry, final long id) {
        return findStoredAccount(entry, id).isPresent();
    }
}
