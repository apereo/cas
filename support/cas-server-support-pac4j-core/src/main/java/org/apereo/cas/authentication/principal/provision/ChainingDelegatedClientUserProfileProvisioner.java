package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;

import lombok.RequiredArgsConstructor;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;

import java.util.List;

/**
 * This is {@link ChainingDelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class ChainingDelegatedClientUserProfileProvisioner extends BaseDelegatedClientUserProfileProvisioner {
    private final List<DelegatedClientUserProfileProvisioner> provisioners;

    @Override
    public void execute(final Principal principal, final UserProfile profile, final BaseClient client) {
        provisioners.forEach(provisioner -> provisioner.execute(principal, profile, client));
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return provisioners.size();
    }

    /**
     * Is empty ?.
     *
     * @return true/false
     */
    public boolean isEmpty() {
        return size() == 0;
    }
}
