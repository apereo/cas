package org.apereo.cas.web.flow.login;

import module java.base;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitialAuthenticationRequestValidationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class InitialAuthenticationRequestValidationAction extends BaseCasWebflowAction {

    private final CasWebflowEventResolver initialAuthenticationProviderWebflowEventResolver;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        return initialAuthenticationProviderWebflowEventResolver.resolveSingle(requestContext);
    }
}
