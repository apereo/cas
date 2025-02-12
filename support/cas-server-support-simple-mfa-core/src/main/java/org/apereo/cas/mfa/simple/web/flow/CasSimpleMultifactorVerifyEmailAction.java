package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasSimpleMultifactorVerifyEmailAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasSimpleMultifactorVerifyEmailAction extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {

    /**
     * Token property to hold the email address that one wants to register.
     */
    public static final String TOKEN_PROPERTY_EMAIL_TO_REGISTER = "emailAddressToRegister";

    private static final String ERROR_CODE_EMAIL_FAILED = "cas.mfa.simple.registration.email.failed";

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
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val emailAddress = requestContext.getRequestParameters().getRequired("email");
            if (isAcceptableEmailAddress(emailAddress)) {
                val authentication = WebUtils.getAuthentication(requestContext);
                val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
                LOGGER.debug("Received email address [{}] for [{}]", emailAddress, principal.getId());
                val token = getOrCreateToken(requestContext, principal);
                token.putProperty(TOKEN_PROPERTY_EMAIL_TO_REGISTER, emailAddress);
                val cmd = CasSimpleMultifactorSendEmail.of(communicationsManager, properties, tenantExtractor);
                val emailSent = cmd.send(principal, token, List.of(emailAddress), requestContext).isAnyEmailSent();
                if (emailSent) {
                    LOGGER.debug("Email [{}] is sent to [{}]", emailAddress, principal.getId());
                    storeToken(requestContext, token);
                    return success(token);
                }
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, ERROR_CODE_EMAIL_FAILED);
        return error();
    }

    protected boolean isAcceptableEmailAddress(final String emailAddress) {
        return EmailValidator.getInstance().isValid(emailAddress)
            && properties.getMail().isRegistrationEnabled()
            && RegexUtils.matches(properties.getMail().getAcceptedEmailPattern(), emailAddress);
    }

    private String getThrottledRequestKeyFor(final Authentication authentication,
                                             final RequestContext requestContext) {
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        return principal.getId();
    }

    protected CasSimpleMultifactorAuthenticationTicket getOrCreateToken(final RequestContext requestContext, final Principal principal) {
        val currentToken = MultifactorAuthenticationWebflowUtils.getSimpleMultifactorAuthenticationToken(requestContext, CasSimpleMultifactorAuthenticationTicket.class);
        return Optional.ofNullable(currentToken)
            .filter(token -> !token.isExpired())
            .orElseGet(Unchecked.supplier(() -> {
                MultifactorAuthenticationWebflowUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
                val service = WebUtils.getService(requestContext);
                return multifactorAuthenticationService.generate(principal, service);
            }));
    }

    protected void storeToken(final RequestContext requestContext, final CasSimpleMultifactorAuthenticationTicket token) throws Throwable {
        multifactorAuthenticationService.store(token);
        MultifactorAuthenticationWebflowUtils.putSimpleMultifactorAuthenticationToken(requestContext, token);
    }
}
