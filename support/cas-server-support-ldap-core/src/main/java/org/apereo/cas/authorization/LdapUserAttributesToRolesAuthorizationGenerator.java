package org.apereo.cas.authorization;

import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.exception.AccountNotFoundException;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Provides a simple {@link AuthorizationGenerator} implementation that obtains user roles from an LDAP search.
 * Searches are performed by this component for every user details lookup:
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #allowMultipleResults} property may be toggled to change that behavior.</li>
 * </ol>
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class LdapUserAttributesToRolesAuthorizationGenerator implements AuthorizationGenerator<CommonProfile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUserAttributesToRolesAuthorizationGenerator.class);

    /**
     * LDAP connection factory.
     */
    protected final ConnectionFactory connectionFactory;
    
    private final SearchExecutor userSearchExecutor;
    private final String roleAttribute;
    private final String rolePrefix;

    /**
     * Flag that indicates whether multiple search results are allowed for a given credential.
     */
    private final boolean allowMultipleResults;

    /**
     * Creates a new instance with the given required parameters.
     *
     * @param factory              Source of LDAP connections for searches.
     * @param userSearchExecutor   Executes the LDAP search for user data.
     * @param allowMultipleResults allow multiple search results in which case the first result
     *                             returned is used to construct user details, or false to indicate that
     *                             a runtime exception should be raised on multiple search results for user details.
     * @param roleAttribute        the role attribute
     * @param rolePrefix           the role prefix
     */
    public LdapUserAttributesToRolesAuthorizationGenerator(final ConnectionFactory factory, 
                                                           final SearchExecutor userSearchExecutor, 
                                                           final boolean allowMultipleResults,
                                                           final String roleAttribute, 
                                                           final String rolePrefix) {
        this.connectionFactory = factory;
        this.userSearchExecutor = userSearchExecutor;
        this.allowMultipleResults = allowMultipleResults;
        this.roleAttribute = roleAttribute;
        this.rolePrefix = rolePrefix;
    }

    @Override
    public void generate(final CommonProfile profile) {
        Assert.notNull(this.connectionFactory, "connectionFactory must not be null");
        Assert.notNull(this.userSearchExecutor, "userSearchExecutor must not be null");

        final String username = profile.getId();
        final SearchResult userResult;
        try {
            LOGGER.debug("Attempting to get details for user [{}].", username);
            final Response<SearchResult> response = this.userSearchExecutor.search(
                    this.connectionFactory,
                    Beans.newSearchFilter(this.userSearchExecutor.getSearchFilter().getFilter(),
                            Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, Arrays.asList(username)));

            LOGGER.debug("LDAP user search response: [{}]", response);
            userResult = response.getResult();

            if (userResult.size() == 0) {
                throw new RuntimeException(new AccountNotFoundException(username + " not found."));
            }
            if (userResult.size() > 1 && !this.allowMultipleResults) {
                throw new IllegalStateException(
                        "Found multiple results for user which is not allowed (allowMultipleResults=false).");
            }

            final LdapEntry userEntry = userResult.getEntry();
            if (userEntry.getAttributes().isEmpty()) {
                throw new IllegalStateException("No attributes are retrieved for this user.");
            }

            final LdapAttribute attribute = userEntry.getAttribute(this.roleAttribute);
            if (attribute == null) {
                throw new IllegalStateException("Configured role attribute cannot be found for this user");
            }

            addProfileRoles(userEntry, profile, attribute);

        } catch (final LdapException e) {
            throw new RuntimeException("LDAP error fetching details for user.", e);
        }
    }

    /**
     * Add profile roles.
     *
     * @param userEntry the user entry
     * @param profile   the profile
     * @param attribute the attribute
     */
    protected void addProfileRoles(final LdapEntry userEntry, final CommonProfile profile, final LdapAttribute attribute) {
        addProfileRolesFromAttributes(profile, attribute, this.rolePrefix);
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
        ldapAttribute.getStringValues().forEach(value -> {
            profile.addRole(prefix.concat(value.toUpperCase()));
            profile.addAttribute(ldapAttribute.getName(), value);
        });
    }
}
