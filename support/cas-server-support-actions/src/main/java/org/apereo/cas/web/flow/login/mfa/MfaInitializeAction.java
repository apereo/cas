package org.apereo.cas.web.flow.login.mfa;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
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
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final String activeFlow = context.getActiveFlow().getId();

        final MultifactorAuthenticationProvider provider =
                ApplicationContextProvider.getApplicationContext()
                        .getBeansOfType(MultifactorAuthenticationProvider.class)
                        .entrySet().stream().filter(e -> e.getKey().startsWith(activeFlow))
                        .map(e -> e.getValue())
                        .findFirst().get();
        if (provider != null) {
            context.getFlowScope().put("provider", provider);
            return success();
        }

        return error();
    }

}
