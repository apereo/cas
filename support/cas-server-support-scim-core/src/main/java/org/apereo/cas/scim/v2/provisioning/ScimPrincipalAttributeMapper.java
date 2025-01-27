package org.apereo.cas.scim.v2.provisioning;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import de.captaingoldfish.scim.sdk.common.resources.User;

/**
 * This is {@link ScimPrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface ScimPrincipalAttributeMapper {
    /**
     * Map the user resource values and attributes from the given principal object.
     *
     * @param user       the user resource
     * @param principal  the current principal as the source object
     * @param credential the current credential being used/validated
     */
    void map(User user, Principal principal, Credential credential);
}
