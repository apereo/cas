package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 2773808902502739L;

    private final transient WatchableGroovyScriptResource watchableScript;
    private final transient ApplicationContext applicationContext;

    public GroovyAcceptableUsagePolicyRepository(final Resource groovyResource, final ApplicationContext applicationContext) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
        this.applicationContext = applicationContext;
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        return watchableScript.execute("verify", AcceptableUsagePolicyStatus.class,
            requestContext, credential, applicationContext, principal, LOGGER);
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        return watchableScript.execute("submit", Boolean.class, requestContext,
            credential, applicationContext, principal, LOGGER);
    }
}
