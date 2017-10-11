package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credential such as client certificates, NTLM, etc.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractNonInteractiveCredentialsAction extends AbstractAuthenticationAction {
    private static final String BAD_X509_CREDENTIALS_MSG_CODE = "error.x509.credentials.bad";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNonInteractiveCredentialsAction.class);

    public AbstractNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                   final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                   final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        final Credential credential = constructCredentialsFromRequest(context);
        if (credential == null) {
            LOGGER.warn("No credentials detected. Navigating to error...");
            return error();
        }
        WebUtils.putCredential(context, credential);
        return super.doPreExecute(context);
    }

    @Override
    protected void onError(final RequestContext requestContext) {
        final MessageResolver resolver = new MessageBuilder()
                .error()
                .code(BAD_X509_CREDENTIALS_MSG_CODE)
                .defaultText(BAD_X509_CREDENTIALS_MSG_CODE)
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
