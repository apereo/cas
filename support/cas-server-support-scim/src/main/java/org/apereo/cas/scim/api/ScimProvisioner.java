package org.apereo.cas.scim.api;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;

/**
 * This is {@link ScimProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface ScimProvisioner {

    /**
     * Create principal.
     *
     * @param p          the principal
     * @param credential the credential
     * @return true/false
     */
    boolean create(Principal p, UsernamePasswordCredential credential);
}
