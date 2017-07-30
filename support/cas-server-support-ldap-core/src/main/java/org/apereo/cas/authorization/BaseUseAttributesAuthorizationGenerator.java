package org.apereo.cas.authorization;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.AccountNotFoundException;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This is {@link BaseUseAttributesAuthorizationGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseUseAttributesAuthorizationGenerator implements AuthorizationGenerator<CommonProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUseAttributesAuthorizationGenerator.class);

    /**
     * Search connection factory.
     */
    protected final ConnectionFactory connectionFactory;

    private final SearchExecutor userSearchExecutor;
    private final boolean allowMultipleResults;

    public BaseUseAttributesAuthorizationGenerator(final ConnectionFactory connectionFactory,
                                                   final SearchExecutor userSearchExecutor,
                                                   final boolean allowMultipleResults) {
        this.connectionFactory = connectionFactory;
        this.userSearchExecutor = userSearchExecutor;
        this.allowMultipleResults = allowMultipleResults;
    }

    /**
     * Add profile roles.
     *
     * @param userEntry the user entry
     * @param profile   the profile
     * @param attribute the attribute
     * @param prefix    the prefix
     */
    protected void addProfileRoles(final LdapEntry userEntry, final CommonProfile profile,
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
    protected void addProfileRolesFromAttributes(final CommonProfile profile,
                                                 final LdapAttribute ldapAttribute,
                                                 final String prefix) {
        ldapAttribute.getStringValues().forEach(value -> profile.addRole(prefix.concat(value.toUpperCase())));
    }

    @Override
    public CommonProfile generate(final WebContext context, final CommonProfile profile) {
        Assert.notNull(this.connectionFactory, "connectionFactory must not be null");
        Assert.notNull(this.userSearchExecutor, "userSearchExecutor must not be null");

        final String username = profile.getId();
        final SearchResult userResult;
        try {
            LOGGER.debug("Attempting to get details for user [{}].", username);
            final Response<SearchResult> response = this.userSearchExecutor.search(
                    this.connectionFactory,
                    LdapUtils.newLdaptiveSearchFilter(this.userSearchExecutor.getSearchFilter().getFilter(),
                            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(username)));

            LOGGER.debug("LDAP user search response: [{}]", response);
            userResult = response.getResult();

            if (userResult.size() == 0) {
                throw new IllegalArgumentException(new AccountNotFoundException(username + " not found."));
            }
            if (userResult.size() > 1 && !this.allowMultipleResults) {
                throw new IllegalStateException(
                        "Found multiple results for user which is not allowed (allowMultipleResults=false).");
            }

            final LdapEntry userEntry = userResult.getEntry();
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
    protected CommonProfile generateAuthorizationForLdapEntry(final CommonProfile profile, final LdapEntry userEntry) {
        return profile;
    }
}
