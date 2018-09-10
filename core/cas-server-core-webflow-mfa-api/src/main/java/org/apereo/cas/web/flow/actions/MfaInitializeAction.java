package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This Action is wired in by AbstractCasMultifactorWebflowConfigurer to put
 * the resolved provider in the flow scope.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class MfaInitializeAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val activeFlow = context.getActiveFlow().getId();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
        val provider = providers.stream().filter(p -> p.getId().startsWith(activeFlow)).findFirst();
        if (provider.isPresent()) {
            context.getFlowScope().put("provider", provider.get());
            return success();
        }
        return error();
    }

}

