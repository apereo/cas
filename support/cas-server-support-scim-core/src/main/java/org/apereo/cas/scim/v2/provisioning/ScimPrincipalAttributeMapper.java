package org.apereo.cas.scim.v2.provisioning;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import de.captaingoldfish.scim.sdk.common.resources.Group;
import de.captaingoldfish.scim.sdk.common.resources.User;

/**
 * This is {@link ScimPrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface ScimPrincipalAttributeMapper {
    /**
     * Map the user resource values and attributes from the given principal object.
     *
     * @param principal  the current principal as the source object
     * @param credential the current credential being used/validated
     * @return the user
     */
    User forCreate(Principal principal, Credential credential);

    /**
     * For update user.
     *
     * @param user       the user
     * @param principal  the principal
     * @param credential the credential
     * @return the user
     */
    User forUpdate(User user, Principal principal, Credential credential);

    /**
     * Map groups list.
     *
     * @param principal the principal
     * @param users     the users
     * @return the list
     */
    List<Group> forGroups(Principal principal, User... users);
}
