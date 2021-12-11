package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import com.unboundid.scim2.common.types.UserResource;

/**
 * This is {@link ScimV2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface ScimV2PrincipalAttributeMapper {
    /**
     * Map the user resource values and attributes from the given principal object.
     *
     * @param user       the user resource
     * @param principal  the current principal as the source object
     * @param credential the current credential being used/validated
     */
    void map(UserResource user, Principal principal, Credential credential);
}
