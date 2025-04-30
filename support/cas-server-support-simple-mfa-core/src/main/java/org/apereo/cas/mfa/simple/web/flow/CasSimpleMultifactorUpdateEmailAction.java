package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorUpdateEmailAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasSimpleMultifactorUpdateEmailAction extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {

    private static final String ERROR_CODE_TOKEN_FAILED = "cas.mfa.simple.registration.token.failed";
    
    protected final CommunicationsManager communicationsManager;

    protected final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    protected final CasSimpleMultifactorAuthenticationProperties properties;

    protected final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    protected final BucketConsumer bucketConsumer;

    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        val result = bucketConsumer.consume(getThrottledRequestKeyFor(authentication, requestContext));
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed() ? super.doPreExecute(requestContext) : error();
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        try {
            val token = requestContext.getRequestParameters().getRequired("token");
            val tokenCredential = new CasSimpleMultifactorTokenCredential(token);
            val ticket = multifactorAuthenticationService.getMultifactorAuthenticationTicket(tokenCredential);
            val credentialPrincipal = multifactorAuthenticationService.getPrincipalFromTicket(ticket);
            val resolvedPrincipal = resolvePrincipal(credentialPrincipal, requestContext);
            val emailAddress = ticket.getProperty(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, String.class);
            val principal = multifactorAuthenticationService.validate(resolvedPrincipal, tokenCredential);
            LOGGER.debug("Updating email address [{}] for [{}]", emailAddress, principal.getId());
            multifactorAuthenticationService.update(principal,
                Map.of(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, emailAddress));
            MultifactorAuthenticationWebflowUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_RESUME,
                new LocalAttributeMap<>(Map.of(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, emailAddress)));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        WebUtils.addErrorMessageToContext(requestContext, ERROR_CODE_TOKEN_FAILED);
        return error();
    }

}
