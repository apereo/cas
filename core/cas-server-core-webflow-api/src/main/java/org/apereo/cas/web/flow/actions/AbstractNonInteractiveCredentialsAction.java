package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credential such as client certificates, NTLM, etc.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public abstract class AbstractNonInteractiveCredentialsAction extends AbstractAuthenticationAction {

    private static final String AUTHN_FAILURE_MESSAGE_CODE = "authenticationFailure.FailedLoginException";

    public AbstractNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                   final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                   final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        val credential = constructCredentialsFromRequest(context);
        if (credential == null) {
            LOGGER.warn("No credentials detected. Navigating to error...");
            return error();
        }
        WebUtils.putCredential(context, credential);
        return super.doPreExecute(context);
    }

    @Override
    protected void onError(final RequestContext requestContext) {
        val resolver = new MessageBuilder()
            .error()
            .code(AUTHN_FAILURE_MESSAGE_CODE)
            .defaultText(AUTHN_FAILURE_MESSAGE_CODE)
            .build();
        requestContext.getMessageContext().addMessage(resolver);
    }

    /**
     * Abstract method to implement to construct the credential from the
     * request object.
     *
     * @param context the context for this request.
     * @return the constructed credential or null if none could be constructed
     * from the request.
     */
    protected abstract Credential constructCredentialsFromRequest(RequestContext context);
}
