package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link GrouperMultifactorAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GrouperMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        return null;
    }


}
