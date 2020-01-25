package org.apereo.cas.authorization;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.profile.UserProfile;

import java.util.Optional;

/**
 * Provides a simple {@link AuthorizationGenerator} implementation that obtains user roles from an LDAP search.
 * Two searches are performed by this component for every user details lookup:
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #isAllowMultipleResults()} property may be toggled to change that behavior.</li>
 * <li>Search for groups of which the user is a member. This search commonly occurs on a separate directory
 * branch than that of the user search.</li>
 * </ol>
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class LdapUserGroupsToRolesAuthorizationGenerator extends BaseUseAttributesAuthorizationGenerator {

    private final String groupAttributeName;

    private final String groupPrefix;

    private final SearchOperation groupSearchOperation;

    /**
     * Instantiates a new Ldap user groups to roles authorization generator.
     *
     * @param userSearchOperation  the user search executor
     * @param allowMultipleResults the allow multiple results
     * @param groupAttributeName   the group attribute name
     * @param groupPrefix          the group prefix
     * @param groupSearchOperation the group search executor
     */
    public LdapUserGroupsToRolesAuthorizationGenerator(final SearchOperation userSearchOperation,
                                                       final boolean allowMultipleResults,
                                                       final String groupAttributeName,
                                                       final String groupPrefix,
                                                       final SearchOperation groupSearchOperation) {
        super(userSearchOperation, allowMultipleResults);
        this.groupAttributeName = groupAttributeName;
        this.groupPrefix = groupPrefix;
        this.groupSearchOperation = groupSearchOperation;
    }

    @Override
    protected Optional<UserProfile> generateAuthorizationForLdapEntry(final UserProfile profile, final LdapEntry userEntry) {
        try {
            LOGGER.debug("Attempting to get roles for user [{}].", userEntry.getDn());
            val response = this.groupSearchOperation.execute(
                LdapUtils.newLdaptiveSearchFilter(this.groupSearchOperation.getTemplate().getFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(userEntry.getDn())));
            LOGGER.debug("LDAP role search response: [{}]", response);

            for (val entry : response.getEntries()) {
                val groupAttribute = entry.getAttribute(this.groupAttributeName);
                if (groupAttribute == null) {
                    LOGGER.warn("Role attribute not found on entry [{}]", entry);
                    continue;
                }
                addProfileRolesFromAttributes(profile, groupAttribute, this.groupPrefix);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("LDAP error fetching roles for user.", e);
        }
        return Optional.ofNullable(profile);
    }
}
