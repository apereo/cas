package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action used to find a put the Duo provider for the active flow in the conversation scope.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@RequiredArgsConstructor
public class DuoInitializeLoginAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;

    @Override
    public Event doExecute(final RequestContext context) {
        final String activeFlow = context.getActiveFlow().getId();

        final DuoMultifactorAuthenticationProvider duoProvider
                = this.provider.findProvider(activeFlow, DuoMultifactorAuthenticationProvider.class);
        WebUtils.putActiveMultifactorAuthenticationProvider(context, duoProvider);
        return success();
    }
}
