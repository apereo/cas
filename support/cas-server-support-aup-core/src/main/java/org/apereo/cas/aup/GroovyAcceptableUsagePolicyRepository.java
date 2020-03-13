package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link GroovyAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 2773808902502739L;

    private final transient WatchableGroovyScriptResource watchableScript;

    private final transient ApplicationContext applicationContext;

    public GroovyAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                 final AcceptableUsagePolicyProperties aupProperties,
                                                 final WatchableGroovyScriptResource watchableScript,
                                                 final ApplicationContext applicationContext) {
        super(ticketRegistrySupport, aupProperties);
        this.watchableScript = watchableScript;
        this.applicationContext = applicationContext;
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        return watchableScript.execute("verify", AcceptableUsagePolicyStatus.class,
            requestContext, credential, applicationContext, principal, LOGGER);
    }

    @Override
    public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext, final Credential credential) {
        try {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val result = watchableScript.execute("fetch", AcceptableUsagePolicyTerms.class,
                requestContext, credential, applicationContext, principal, LOGGER);
            return Optional.ofNullable(result);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        return watchableScript.execute("submit", Boolean.class, requestContext,
            credential, applicationContext, principal, LOGGER);
    }
}
