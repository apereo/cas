package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyDelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyDelegatedClientUserProfileProvisioner extends BaseDelegatedClientUserProfileProvisioner {
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyDelegatedClientUserProfileProvisioner(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public void execute(final Principal principal, final CommonProfile profile, final BaseClient client) {
        val args = new Object[]{principal, profile, client, LOGGER};
        watchableScript.execute(args, Void.class);
    }
}
