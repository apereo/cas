package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * This is {@link AbstractTerminatingAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public abstract class AbstractTerminatingAuthenticationWebflowAction extends AbstractAction {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        try {
            final Credential credential = WebUtils.getCredential(requestContext);
            final Service service = WebUtils.getService(requestContext);
            final AuthenticationResultBuilder builder = WebUtils.getAuthenticationContextBuilder(requestContext);
            final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(credential);
            this.authenticationSystemSupport.getAuthenticationTransactionManager().handle(transaction, builder);
            final AuthenticationResult authenticationContext = builder.build(service);
            return resolveSuccessfulAuthenticationEvent(requestContext, authenticationContext);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return new Event(this, "error");
        }
    }

    /**
     * Resolve successful authentication event event.
     *
     * @param requestContext        the request context
     * @param authenticationContext the authentication context
     * @return the event
     */
    protected abstract Event resolveSuccessfulAuthenticationEvent(final RequestContext requestContext,
                                                        final AuthenticationResult authenticationContext);
}
