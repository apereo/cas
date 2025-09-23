package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;

/**
 * This is {@link GroovyDelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class GroovyDelegatedClientAuthenticationCredentialResolver
    extends BaseDelegatedClientAuthenticationCredentialResolver implements DisposableBean {
    private final ExecutableCompiledScript watchableScript;

    public GroovyDelegatedClientAuthenticationCredentialResolver(
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final Resource groovyResource) {
        super(configContext);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public List<DelegatedAuthenticationCandidateProfile> resolve(final RequestContext context,
                                                                 final ClientCredential credentials) throws Throwable {
        val profile = resolveUserProfile(context, credentials);
        val args = new Object[]{context, credentials, profile, LOGGER};
        return watchableScript.execute(args, List.class);
    }

    @Override
    public void destroy() {
        watchableScript.close();
    }
}
