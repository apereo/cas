package org.jasig.cas.userdetails;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Provides a simple {@link UserDetailsService} implementation that obtains user details from an LDAP search.
 * Two searches are performed by this component for every user details lookup:
 *
 * <ol>
 *     <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 *     but the {@link #setAllowMultipleResults(boolean)} property may be toggled to change that behavior.</li>
 *     <li>Search for groups of which the user is a member. This search commonly occurs on a separate directory
 *     branch than that of the user search.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Component("ldapUserDetailsService")
public class LdapUserDetailsService implements UserDetailsService {

    /** Default role prefix. */
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    /** Placeholder for unknown password given to user details. */
    public static final String UNKNOWN_PASSWORD = "<UNKNOWN>";

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Source of LDAP connections. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapUserDetailsServiceConnectionFactory")
    private ConnectionFactory connectionFactory;

    /** Executes the search query for user data. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapUserDetailsServiceUserSearchExecutor")
    private SearchExecutor userSearchExecutor;

    /** Executes the search query for roles. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapUserDetailsServiceRoleSearchExecutor")
    private SearchExecutor roleSearchExecutor;

    /** Specify the name of LDAP attribute to use as principal identifier. */
    @NotNull
    @Value("${ldap.userdetails.service.user.attr:}")
    private String userAttributeName;

    /** Specify the name of LDAP attribute to be used as the basis for role granted authorities. */
    @NotNull
    @Value("${ldap.userdetails.service.role.attr:}")
    private String roleAttributeName;

    /** Prefix appended to the uppercased
     * {@link #roleAttributeName} per the normal Spring Security convention.
     **/
    @NotNull
    @Value("${ldap.userdetails.service.role.prefix:" + DEFAULT_ROLE_PREFIX + "}")
    private String rolePrefix = DEFAULT_ROLE_PREFIX;

    /** Flag that indicates whether multiple search results are allowed for a given credential. */
    @Value("${ldap.userdetails.service.allow.multiple:false}")
    private boolean allowMultipleResults;

    /**
     * Instantiates a new Ldap user details service.
     */
    public LdapUserDetailsService() {}

    /**
     * Creates a new instance with the given required parameters.
     *
     * @param  factory  Source of LDAP connections for searches.
     * @param  userSearchExecutor  Executes the LDAP search for user data.
     * @param roleSearchExecutor  Executes the LDAP search for role data.
     * @param userAttributeName  Name of LDAP attribute that contains username for user details.
     * @param roleAttributeName  Name of LDAP attribute that contains role membership data for the user.
     */
    public LdapUserDetailsService(
            final ConnectionFactory factory,
            final SearchExecutor userSearchExecutor,
            final SearchExecutor roleSearchExecutor,
            final String userAttributeName,
            final String roleAttributeName) {

        this.connectionFactory = factory;
        this.userSearchExecutor = userSearchExecutor;
        this.roleSearchExecutor = roleSearchExecutor;
        this.userAttributeName = userAttributeName;
        this.roleAttributeName = roleAttributeName;
    }


    /**
     * Sets the prefix appended to the uppercase {@link #roleAttributeName} per the normal Spring Security convention.
     * The default value {@value #DEFAULT_ROLE_PREFIX} is sufficient in most cases.
     *
     * @param  rolePrefix  Role prefix.
     */
    public void setRolePrefix(final String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }


    /**
     * Sets whether to allow multiple search results for user details given a username.
     * This is false by default, which is sufficient and secure for more deployments.
     * Setting this to true may have security consequences.
     *
     * @param  allowMultiple  True to allow multiple search results in which case the first result
     *                        returned is used to construct user details, or false to indicate that
     *                        a runtime exception should be raised on multiple search results for user details.
     */
    public void setAllowMultipleResults(final boolean allowMultiple) {
        this.allowMultipleResults = allowMultiple;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) {
        final SearchResult userResult;
        try {
            logger.debug("Attempting to get details for user {}.", username);
            final Response<SearchResult> response = this.userSearchExecutor.search(
                    this.connectionFactory,
                    createSearchFilter(this.userSearchExecutor, username));
            logger.debug("LDAP user search response: {}", response);
            userResult = response.getResult();
        } catch (final LdapException e) {
            throw new RuntimeException("LDAP error fetching details for user.", e);
        }
        if (userResult.size() == 0) {
            throw new UsernameNotFoundException(username + " not found.");
        }
        if (userResult.size() > 1 && !this.allowMultipleResults) {
            throw new IllegalStateException(
                    "Found multiple results for user which is not allowed (allowMultipleResults=false).");
        }
        final LdapEntry userResultEntry = userResult.getEntry();
        final String userDn = userResultEntry.getDn();
        final LdapAttribute userAttribute = userResultEntry.getAttribute(this.userAttributeName);
        if (userAttribute == null) {
            throw new IllegalStateException(this.userAttributeName + " attribute not found in results.");
        }
        final String id = userAttribute.getStringValue();

        final SearchResult roleResult;
        try {
            logger.debug("Attempting to get roles for user {}.", userDn);
            final Response<SearchResult> response = this.roleSearchExecutor.search(
                    this.connectionFactory,
                    createSearchFilter(this.roleSearchExecutor, userDn));
            logger.debug("LDAP role search response: {}", response);
            roleResult = response.getResult();
        } catch (final LdapException e) {
            throw new RuntimeException("LDAP error fetching roles for user.", e);
        }
        LdapAttribute roleAttribute;
        final Collection<SimpleGrantedAuthority> roles = new ArrayList<>(roleResult.size());
        for (final LdapEntry entry : roleResult.getEntries()) {
            roleAttribute = entry.getAttribute(this.roleAttributeName);
            if (roleAttribute == null) {
                logger.warn("Role attribute not found on entry {}", entry);
                continue;
            }

            for (final String role : roleAttribute.getStringValues()) {
                roles.add(new SimpleGrantedAuthority(this.rolePrefix + role.toUpperCase()));
            }

        }

        return new User(id, UNKNOWN_PASSWORD, roles);
    }

    /**
     * Constructs a new search filter using {@link SearchExecutor#searchFilter} as a template and
     * the username as a parameter.
     *
     * @param executor the executor
     * @param username the username
     * @return  Search filter with parameters applied.
     */
    private SearchFilter createSearchFilter(final SearchExecutor executor, final String username) {
        final SearchFilter filter = new SearchFilter();
        filter.setFilter(executor.getSearchFilter().getFilter());
        filter.setParameter(0, username);

        logger.debug("Constructed LDAP search filter [{}]", filter.format());
        return filter;
    }
}
