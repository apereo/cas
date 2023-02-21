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
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
                                final Ticket tokenTicket,
                                final RequestContext requestContext) {
        return FunctionUtils.doIf(communicationsManager.isSmsSenderDefined(),
            () -> {
                val smsProperties = properties.getSms();
                val token = tokenTicket.getId();
                val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
                val smsText = StringUtils.isNotBlank(smsProperties.getText())
                    ? SmsBodyBuilder.builder().properties(smsProperties).parameters(
                        Map.of("token", token, "tokenWithoutPrefix", tokenWithoutPrefix))
                    .build().get()
                    : token;

                val smsRequest = SmsRequest.builder().from(smsProperties.getFrom())
                    .principal(principal).attribute(smsProperties.getAttributeName())
                    .text(smsText).build();
                return communicationsManager.sms(smsRequest);
            }, () -> false).get();
    }

    protected List<EmailCommunicationResult> isMailSent(final CommunicationsManager communicationsManager,
                                                        final CasSimpleMultifactorAuthenticationProperties properties,
                                                        final Principal principal, final Ticket tokenTicket,
                                                        final RequestContext requestContext) {
        return FunctionUtils.doIf(communicationsManager.isMailSenderDefined(),
            () -> {
                val body = prepareEmailMessageBody(principal, tokenTicket, requestContext, properties);
                return properties.getMail().getAttributeName()
                    .stream()
                    .map(attribute -> sendEmail(communicationsManager, properties, requestContext, body, principal, attribute))
                    .collect(Collectors.toList());
            }, () -> List.<EmailCommunicationResult>of(EmailCommunicationResult.builder().success(false).build())).get();
    }

    protected EmailCommunicationResult sendEmail(final CommunicationsManager communicationsManager,
                                                 final CasSimpleMultifactorAuthenticationProperties properties,
                                                 final RequestContext requestContext,
                                                 final EmailMessageBodyBuilder body,
                                                 final Principal principal,
                                                 final String attribute) {

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val emailRequest = EmailMessageRequest.builder()
            .emailProperties(properties.getMail())
            .locale(locale.orElseGet(Locale::getDefault))
            .principal(principal)
            .attribute(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute))
            .body(body.get())
            .build();
        return communicationsManager.email(emailRequest);
    }

    protected EmailMessageBodyBuilder prepareEmailMessageBody(final Principal principal,
                                                              final Ticket tokenTicket,
                                                              final RequestContext requestContext,
                                                              final CasSimpleMultifactorAuthenticationProperties properties) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val parameters = CoreAuthenticationUtils.convertAttributeValuesToObjects(principal.getAttributes());
        val token = tokenTicket.getId();
        val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
        parameters.put("token", token);
        parameters.put("tokenWithoutPrefix", tokenWithoutPrefix);

        return EmailMessageBodyBuilder.builder()
            .properties(properties.getMail())
            .locale(locale)
            .parameters(parameters)
            .build();
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
                        && isMailSent(communicationsManager, properties, principal, token, requestContext)
                            .stream().anyMatch(EmailCommunicationResult::isSuccess);

        val notificationSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.NOTIFICATION)
                               && isNotificationSent(communicationsManager, principal, token);

        if (smsSent || emailSent || notificationSent) {
            LOGGER.debug("Successfully submitted token via strategy option [{}] to [{}]", strategy, principal.getId());
            storeToken(requestContext, token);
            return buildSuccessEvent(token);
        }
        LOGGER.error("Communication strategies failed to submit token [{}] to user", token.getId());
        return error();
    }

    protected Event buildSuccessEvent(final CasSimpleMultifactorAuthenticationTicket token) {
        val attributes = new LocalAttributeMap<Object>("token", token.getId());
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
    }

    protected void storeToken(final RequestContext requestContext, final CasSimpleMultifactorAuthenticationTicket token) throws Exception {
        multifactorAuthenticationService.store(token);
        WebUtils.addInfoMessageToContext(requestContext, MESSAGE_MFA_TOKEN_SENT);
        WebUtils.putSimpleMultifactorAuthenticationToken(requestContext, token);
    }

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
