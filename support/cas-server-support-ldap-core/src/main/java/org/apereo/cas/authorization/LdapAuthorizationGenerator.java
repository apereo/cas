package org.apereo.cas.authorization;

import org.apereo.cas.configuration.support.Beans;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
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

import javax.annotation.Nullable;

/**
 * Provides a simple {@link AuthorizationGenerator} implementation that obtains user roles from an LDAP search.
 * Two searches are performed by this component for every user details lookup:
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #setAllowMultipleResults(boolean)} property may be toggled to change that behavior.</li>
 * <li>Search for groups of which the user is a member. This search commonly occurs on a separate directory
 * branch than that of the user search.</li>
 * </ol>
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class LdapAuthorizationGenerator implements AuthorizationGenerator<CommonProfile> {
    
    private transient Logger logger = LoggerFactory.getLogger(getClass());
    
    private ConnectionFactory connectionFactory;
    
    private SearchExecutor userSearchExecutor;

    private String roleAttribute;

    private String rolePrefix;

    /**
     * Flag that indicates whether multiple search results are allowed for a given credential.
     */
    private boolean allowMultipleResults;

    /**
     * Instantiates a new Ldap authorization generator.
     */
    public LdapAuthorizationGenerator() {
    }

    /**
     * Creates a new instance with the given required parameters.
     *
     * @param factory            Source of LDAP connections for searches.
     * @param userSearchExecutor Executes the LDAP search for user data.
     */
    public LdapAuthorizationGenerator(
            final ConnectionFactory factory,
            final SearchExecutor userSearchExecutor) {
        this.connectionFactory = factory;
        this.userSearchExecutor = userSearchExecutor;
    }


    public void setRolePrefix(final String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }


    /**
     * Sets whether to allow multiple search results for user details given a username.
     * This is false by default, which is sufficient and secure for more deployments.
     * Setting this to true may have security consequences.
     *
     * @param allowMultiple True to allow multiple search results in which case the first result
     *                      returned is used to construct user details, or false to indicate that
     *                      a runtime exception should be raised on multiple search results for user details.
     */
    public void setAllowMultipleResults(final boolean allowMultiple) {
        this.allowMultipleResults = allowMultiple;
    }

    @Override
    public void generate(final CommonProfile profile) {
        Assert.notNull(this.connectionFactory, "connectionFactory must not be null");
        Assert.notNull(this.userSearchExecutor, "userSearchExecutor must not be null");

        final String username = profile.getId();
        final SearchResult userResult;
        try {
            logger.debug("Attempting to get details for user {}.", username);
            final Response<SearchResult> response = this.userSearchExecutor.search(
                    this.connectionFactory,
                    Beans.newSearchFilter(this.userSearchExecutor.getSearchFilter().getFilter(), username));
            
            logger.debug("LDAP user search response: {}", response);
            userResult = response.getResult();

            if (userResult.size() == 0) {
                throw new AccountNotFoundException(username + " not found.");
            }
            if (userResult.size() > 1 && !this.allowMultipleResults) {
                throw new IllegalStateException(
                        "Found multiple results for user which is not allowed (allowMultipleResults=false).");
            }

            if (userResult.getEntry().getAttributes().isEmpty()) {
                throw new IllegalStateException("No attributes are retrieved for this user.");
            }
            
            final LdapAttribute attribute = userResult.getEntry().getAttribute(this.roleAttribute);
            if (attribute == null) {
                throw new IllegalStateException("Configured role attribute cannot be found for this user");
            }

            addProfileRolesFromAttributes(profile, attribute);

        } catch (final LdapException e) {
            throw new RuntimeException("LDAP error fetching details for user.", e);
        }
    }

    /**
     * Add profile roles from attributes.
     *
     * @param profile       the profile
     * @param ldapAttribute the ldap attribute
     */
    protected void addProfileRolesFromAttributes(final CommonProfile profile, final LdapAttribute ldapAttribute) {
        ldapAttribute.getStringValues().stream().forEach(value -> {
            profile.addRole(this.rolePrefix.concat(value.toUpperCase()));
            profile.addAttribute(ldapAttribute.getName(), value);
        });
    }
    
    public void setConnectionFactory(@Nullable final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setUserSearchExecutor(@Nullable final SearchExecutor userSearchExecutor) {
        this.userSearchExecutor = userSearchExecutor;
    }

    public void setRoleAttribute(final String roleAttribute) {
        this.roleAttribute = roleAttribute;
    }
}
