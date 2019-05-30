package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorTokenCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoMultifactorValidateChannelAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorValidateChannelAction extends AbstractAction {
    private final SessionStore<J2EContext> sessionStore;
    private final AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val eventAttributes = new LocalAttributeMap<>();
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new J2EContext(request, response, this.sessionStore);

            val channel = AccepttoWebflowUtils.getChannel(webContext);
            if (channel == null) {
                LOGGER.debug("Unable to determine channel from session store; not a validation attempt");
                return null;
            }
            val authentication = AccepttoWebflowUtils.getAuthentication(webContext);
            if (authentication == null) {
                LOGGER.debug("Unable to determine the original authentication attempt the session store");
                throw new AuthenticationException("Unable to determine authentication from session store");
            }
            WebUtils.putAuthentication(authentication, requestContext);

            val credential = new AccepttoMultifactorTokenCredential(channel.toString());
            val service = WebUtils.getService(requestContext);

            LOGGER.debug("Cleaning up session store to remove [{}]", credential);
            AccepttoWebflowUtils.resetChannelAndAuthentication(webContext);

            LOGGER.debug("Attempting to authenticate channel [{}] with authentication [{}] and service [{}]",
                credential, authentication, service);
            var resultBuilder = authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
            resultBuilder = authenticationSystemSupport.handleAuthenticationTransaction(service, resultBuilder, credential);
            WebUtils.putAuthenticationResultBuilder(resultBuilder, requestContext);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE);
        } catch (final Exception e) {
            eventAttributes.put("error", e);
            LOGGER.error(e.getMessage(), e);
        }
        return new EventFactorySupport().event(this,
            CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, eventAttributes);
    }
}
