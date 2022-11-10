package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link CasSimpleMultifactorSendTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasSimpleMultifactorSendTokenAction extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {
    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final CommunicationsManager communicationsManager;

    private final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    private final CasSimpleMultifactorAuthenticationProperties properties;

    private final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    private final BucketConsumer bucketConsumer;

    protected boolean isSmsSent(final CommunicationsManager communicationsManager,
                                final CasSimpleMultifactorAuthenticationProperties properties,
                                final Principal principal,
                                final Ticket token,
                                final RequestContext requestContext) {
        if (communicationsManager.isSmsSenderDefined()) {
            val smsProperties = properties.getSms();
            val smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? SmsBodyBuilder.builder().properties(smsProperties).parameters(Map.of("token", token.getId())).build().get()
                : token.getId();

            val smsRequest = SmsRequest.builder().from(smsProperties.getFrom())
                .principal(principal).attribute(smsProperties.getAttributeName())
                .text(smsText).build();
            return communicationsManager.sms(smsRequest);
        }
        return false;
    }

    /**
     * Send an email.
     *
     * @param communicationsManager the communication manager
     * @param properties            the properties
     * @param principal             the principal
     * @param token                 the token
     * @param requestContext        the request context
     * @return whether the email has been sent.
     */
    protected EmailCommunicationResult isMailSent(final CommunicationsManager communicationsManager,
                                                  final CasSimpleMultifactorAuthenticationProperties properties,
                                                  final Principal principal, final Ticket token,
                                                  final RequestContext requestContext) {
        if (communicationsManager.isMailSenderDefined()) {
            val mailProperties = properties.getMail();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val parameters = CoreAuthenticationUtils.convertAttributeValuesToObjects(principal.getAttributes());
            parameters.put("token", token.getId());

            val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                .map(resolver -> resolver.resolveLocale(request));
            val body = EmailMessageBodyBuilder.builder()
                .properties(mailProperties)
                .locale(locale)
                .parameters(parameters)
                .build()
                .get();
            val emailRequest = EmailMessageRequest.builder().emailProperties(mailProperties)
                .locale(locale.orElseGet(Locale::getDefault))
                .principal(principal).attribute(mailProperties.getAttributeName())
                .body(body).build();
            return communicationsManager.email(emailRequest);
        }
        return EmailCommunicationResult.builder().build();
    }

    protected boolean isNotificationSent(final CommunicationsManager communicationsManager,
                                         final Principal principal,
                                         final Ticket token) {
        return communicationsManager.isNotificationSenderDefined()
               && communicationsManager.notify(principal, "Apereo CAS Token", String.format("Token: %s", token.getId()));
    }

    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getInProgressAuthentication();
        val result = bucketConsumer.consume(getThrottledRequestKeyFor(authentication));
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed() ? super.doPreExecute(requestContext) : error();
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = resolvePrincipal(authentication.getPrincipal());
        val token = getOrCreateToken(requestContext, principal);
        LOGGER.debug("Using token [{}] created at [{}]", token.getId(), token.getCreationTime());

        val strategy = tokenCommunicationStrategy.determineStrategy(token);
        val smsSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.SMS)
                      && isSmsSent(communicationsManager, properties, principal, token, requestContext);

        val emailSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.EMAIL)
                        && isMailSent(communicationsManager, properties, principal, token, requestContext).isSuccess();

        val notificationSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.NOTIFICATION)
                               && isNotificationSent(communicationsManager, principal, token);

        if (smsSent || emailSent || notificationSent) {
            multifactorAuthenticationService.store(token);
            LOGGER.debug("Successfully submitted token via strategy option [{}] to [{}]", strategy, principal.getId());
            WebUtils.addInfoMessageToContext(requestContext, MESSAGE_MFA_TOKEN_SENT);
            val attributes = new LocalAttributeMap<Object>("token", token.getId());
            WebUtils.putSimpleMultifactorAuthenticationToken(requestContext, token);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
        }
        LOGGER.error("Communication strategies failed to submit token [{}] to user", token.getId());
        return error();
    }


    /**
     * Get or create a token.
     *
     * @param requestContext the request context
     * @param principal      the principal
     * @return the token
     */
    protected CasSimpleMultifactorAuthenticationTicket getOrCreateToken(final RequestContext requestContext, final Principal principal) {
        val currentToken = WebUtils.getSimpleMultifactorAuthenticationToken(requestContext, CasSimpleMultifactorAuthenticationTicket.class);
        return Optional.ofNullable(currentToken)
            .filter(token -> !token.isExpired())
            .orElseGet(Unchecked.supplier(() -> {
                WebUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
                val service = WebUtils.getService(requestContext);
                return multifactorAuthenticationService.generate(principal, service);
            }));
    }

    private String getThrottledRequestKeyFor(final Authentication authentication) {
        val principal = resolvePrincipal(authentication.getPrincipal());
        return principal.getId();
    }
}
