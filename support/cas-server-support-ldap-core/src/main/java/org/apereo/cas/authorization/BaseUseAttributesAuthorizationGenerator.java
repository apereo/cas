package org.apereo.cas.authorization;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.AccountNotFoundException;
import org.pac4j.core.profile.UserProfile;

import java.util.Optional;

/**
 * This is {@link BaseUseAttributesAuthorizationGenerator}.
 *
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #allowMultipleResults} property may be toggled to change that behavior.</li>
 * </ol>
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseUseAttributesAuthorizationGenerator implements AuthorizationGenerator {

    private final SearchOperation userSearchOperation;
    private final boolean allowMultipleResults;

    /**
     * Add profile roles.
     *
     * @param userEntry the user entry
     * @param profile   the profile
     * @param attribute the attribute
     * @param prefix    the prefix
     */
    protected void addProfileRoles(final LdapEntry userEntry, final UserProfile profile,
                                   final LdapAttribute attribute, final String prefix) {
        addProfileRolesFromAttributes(profile, attribute, prefix);
    }

    /**
     * Add profile roles from attributes.
     *
     * @param profile       the profile
     * @param ldapAttribute the ldap attribute
     * @param prefix        the prefix
     */
    protected void addProfileRolesFromAttributes(final UserProfile profile,
                                                 final LdapAttribute ldapAttribute,
                                                 final String prefix) {
        ldapAttribute.getStringValues().forEach(value -> profile.addRole(prefix.concat(value.toUpperCase())));
    }

    @Override
    public Optional<UserProfile> generate(final WebContext context, final UserProfile profile) {
        val username = profile.getId();

        try {
            LOGGER.debug("Attempting to get details for user [{}].", username);
            val filter = LdapUtils.newLdaptiveSearchFilter(this.userSearchOperation.getTemplate().getFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(username));
            val response = this.userSearchOperation.execute(filter);

            LOGGER.debug("LDAP user search response: [{}]", response);
            if (response.entrySize() == 0) {
                throw new IllegalArgumentException(new AccountNotFoundException(username + " not found."));
            }
            if (!this.allowMultipleResults && response.entrySize() > 1) {
                throw new IllegalStateException("Found multiple results for user which is not allowed.");
            }

            val userEntry = response.getEntry();
            return generateAuthorizationForLdapEntry(profile, userEntry);
        } catch (final LdapException e) {
            throw new IllegalArgumentException("LDAP error fetching details for user.", e);
        }
    }

    /**
     * Generate authorization for ldap entry.
     *
     * @param profile   the profile
     * @param userEntry the user entry
     * @return the common profile
     */
    protected Optional<UserProfile> generateAuthorizationForLdapEntry(final UserProfile profile, final LdapEntry userEntry) {
        return Optional.ofNullable(profile);
    }
}
