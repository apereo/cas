package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * Action used to find a put the Duo provider for the active flow in the conversation scope.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@RequiredArgsConstructor
public class DuoInitializeLoginAction extends AbstractAction {
    private final DefaultAuthenticationEventExecutionPlan plan;

    @Override
    public Event doExecute(final RequestContext context) {
        final String activeFlow = context.getActiveFlow().getId();

        plan.registerAuthenticationHandlerResolver(new AuthenticationHandlerResolver() {
            @Override
            public Set<AuthenticationHandler> resolve(Set<AuthenticationHandler> candidateHandlers, AuthenticationTransaction transaction) {
                return CollectionUtils.wrapSet(candidateHandlers.stream().filter(c -> c.getName().equals(activeFlow)).findFirst().get());
            }
        });
        return success();
    }
}
