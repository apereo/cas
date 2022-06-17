package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link DelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface DelegatedClientUserProfileProvisioner {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "clientUserProfileProvisioner";

    /**
     * Execute.
     *
     * @param principal  the principal
     * @param profile    the profile
     * @param client     the client
     * @param credential the credential
     */
    default void execute(final Principal principal, final UserProfile profile,
                         final BaseClient client, final Credential credential) {
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
