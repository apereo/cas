package org.apereo.cas.web.flow.mfa;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This Action is wired in by AbstractCasMultifactorWebflowConfigurer to put
 * the resolved provider in the flow scope.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class MultifactorAuthenticationInitializeAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final String activeFlow = context.getActiveFlow().getId();

        // Find the provider that matches the flow id
        final Optional<MultifactorAuthenticationProvider> provider =
                applicationContext
                        .getBeansOfType(MultifactorAuthenticationProvider.class)
                        .entrySet().stream().filter(e -> e.getValue().equals(activeFlow))
                        .map(e -> e.getValue())
                        .findFirst();

        if (provider.isPresent()) {
            context.getFlowScope().put("provider", provider.get());
            return success();
        }
        return error();
    }

}
