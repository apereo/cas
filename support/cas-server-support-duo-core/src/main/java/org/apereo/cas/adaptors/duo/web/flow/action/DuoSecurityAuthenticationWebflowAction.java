package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoSecurityAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class DuoSecurityAuthenticationWebflowAction extends BaseCasWebflowAction {

    protected final CasWebflowEventResolver duoAuthenticationWebflowEventResolver;
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        return duoAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
