package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link ScimDelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class ScimDelegatedClientUserProfileProvisioner extends BaseDelegatedClientUserProfileProvisioner {
    private final PrincipalProvisioner provisioner;

    @Override
    public void execute(final Principal principal, final UserProfile profile,
                        final BaseClient client, final Credential credential) {
        val result = provisioner.provision(principal, credential);
        LOGGER.info("Provisioned principal [{}] from external identity provider [{}]: [{}]",
            principal.getId(), profile.getClientName(),
            BooleanUtils.toString(result, "success", "failure"));
    }
}
