package org.apereo.cas.authorization;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.profile.UserProfile;

import java.util.Optional;

/**
 * Provides a simple {@link AuthorizationGenerator} implementation that obtains user roles from an LDAP search.
 * Searches are performed by this component for every user details lookup:
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
@Getter
public class LdapUserAttributesToRolesAuthorizationGenerator extends BaseUseAttributesAuthorizationGenerator {

    private final String roleAttribute;
    private final String rolePrefix;

    /**
     * Creates a new instance with the given required parameters.
     *
     * @param userSearchOperation   Executes the LDAP search for user data.
     * @param allowMultipleResults allow multiple search results in which case the first result
     *                             returned is used to construct user details, or false to indicate that
     *                             a runtime exception should be raised on multiple search results for user details.
     * @param roleAttribute        the role attribute
     * @param rolePrefix           the role prefix
     */
    public LdapUserAttributesToRolesAuthorizationGenerator(final SearchOperation userSearchOperation,
                                                           final boolean allowMultipleResults,
                                                           final String roleAttribute,
                                                           final String rolePrefix) {
        super(userSearchOperation, allowMultipleResults);
        this.roleAttribute = roleAttribute;
        this.rolePrefix = rolePrefix;
    }

    @Override
    protected Optional<UserProfile> generateAuthorizationForLdapEntry(final UserProfile profile, final LdapEntry userEntry) {
        if (!userEntry.getAttributes().isEmpty()) {
            val attribute = userEntry.getAttribute(this.roleAttribute);
            if (attribute != null) {
                addProfileRoles(userEntry, profile, attribute, this.rolePrefix);
            } else {
                LOGGER.warn("Configured role attribute cannot be found for this user");
            }
        } else {
            LOGGER.warn("No attributes are retrieved for this user.");
        }
        return Optional.ofNullable(profile);
    }
}
