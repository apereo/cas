package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;

import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ChainingDelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class ChainingDelegatedClientUserProfileProvisioner extends BaseDelegatedClientUserProfileProvisioner {
    private final List<DelegatedClientUserProfileProvisioner> provisioners = new ArrayList<>();

    @Override
    public void execute(final Principal principal, final CommonProfile profile, final BaseClient client) {
        provisioners.forEach(provisioner -> provisioner.execute(principal, profile, client));
    }

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addProvisioner(final DelegatedClientUserProfileProvisioner policy) {
        this.provisioners.add(policy);
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
     * @return the boolean
     */
    public boolean isEmpty() {
        return size() == 0;
    }
}
