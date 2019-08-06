package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;

/**
 * This is {@link DelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface DelegatedClientUserProfileProvisioner {

    /**
     * Execute.
     *
     * @param principal the principal
     * @param profile   the profile
     * @param client    the client
     */
    default void execute(final Principal principal, final CommonProfile profile, final BaseClient client) {
    }

    /**
     * No op delegated client user profile provisioner.
     *
     * @return the delegated client user profile provisioner
     */
    static DelegatedClientUserProfileProvisioner noOp() {
        return new DelegatedClientUserProfileProvisioner() {
        };
    }
}
