package org.jasig.cas.adaptors.duo;

import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.web.flow.AbstractTerminatingAuthenticationWebflowAction;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoTerminatingAuthenticationWebflowAction }.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoTerminatingAuthenticationWebflowAction")
public class DuoTerminatingAuthenticationWebflowAction  extends AbstractTerminatingAuthenticationWebflowAction {

    @Override
    protected Event resolveSuccessfulAuthenticationEvent(final RequestContext requestContext,
                                                         final AuthenticationResult authenticationContext) {
        return new Event(this, "mfaSuccess");
    }
}
