package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    private final CentralAuthenticationService centralAuthenticationService;

    private final CommunicationsManager communicationsManager;

    private final TicketFactory ticketFactory;

    private final CasSimpleMultifactorAuthenticationProperties properties;

    private final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    private final BucketConsumer bucketConsumer;

    /**
     * Send a SMS.
     *
     * @param communicationsManager the communication manager
     * @param properties            the properties
     * @param principal             the principal
     * @param token                 the token
     * @return whether the SMS has been sent.
     */
    protected boolean isSmsSent(final CommunicationsManager communicationsManager,
                                final CasSimpleMultifactorAuthenticationProperties properties,
                                final Principal principal,
                                final Ticket token) {
        if (communicationsManager.isSmsSenderDefined()) {
            val smsProperties = properties.getSms();
            val smsText = StringUtils.isNotBlank(smsProperties.getText())
                ? smsProperties.getFormattedText(token.getId())
                : token.getId();
            return communicationsManager.sms(principal, smsProperties.getAttributeName(), smsText, smsProperties.getFrom());
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
    protected boolean isMailSent(final CommunicationsManager communicationsManager,
                                 final CasSimpleMultifactorAuthenticationProperties properties,
                                 final Principal principal, final Ticket token,
                                 final RequestContext requestContext) {
        if (communicationsManager.isMailSenderDefined()) {
            val mailProperties = properties.getMail();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val body = EmailMessageBodyBuilder.builder().properties(mailProperties)
                .locale(Optional.ofNullable(request.getLocale()))
                .parameters(Map.of("token", token.getId())).build().produce();
            return communicationsManager.email(principal, mailProperties.getAttributeName(), mailProperties, body);
        }
        return false;
    }

    /**
     * Send a notification.
     *
     * @param communicationsManager the communication manager
     * @param principal             the principal
     * @param token                 the token
     * @return whether the notification has been sent.
     */
    protected boolean isNotificationSent(final CommunicationsManager communicationsManager,
                                         final Principal principal,
                                         final Ticket token) {
        return communicationsManager.isNotificationSenderDefined()
               && communicationsManager.notify(principal, "Apereo CAS Token", String.format("Token: %s", token.getId()));
    }

    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val result = bucketConsumer.consume();
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed() ? super.doPreExecute(requestContext) : error();
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = resolvePrincipal(authentication.getPrincipal());
        val token = getOrCreateToken(requestContext, principal);
        LOGGER.debug("Using token [{}] created at [{}]", token.getId(), token.getCreationTime());

        val strategy = tokenCommunicationStrategy.determineStrategy(token);
        val smsSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.SMS)
                      && isSmsSent(communicationsManager, properties, principal, token);

        val emailSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.EMAIL)
                        && isMailSent(communicationsManager, properties, principal, token, requestContext);

        val notificationSent = strategy.contains(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.NOTIFICATION)
                               && isNotificationSent(communicationsManager, principal, token);

        if (smsSent || emailSent || notificationSent) {
            addOrUpdateToken(token);
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
     * Add or update token.
     *
     * @param token the token
     */
    protected void addOrUpdateToken(final CasSimpleMultifactorAuthenticationTicket token) {
        FunctionUtils.doAndHandle(ticket -> {
            LOGGER.debug("Updating existing token [{}] to registry", token.getId());
            val trackingToken = centralAuthenticationService.getTicket(ticket.getId());
            centralAuthenticationService.updateTicket(trackingToken);
        }, throwable -> {
            LOGGER.trace(throwable.getMessage(), throwable);
            LOGGER.debug("Adding token [{}] to registry", token.getId());
            centralAuthenticationService.addTicket(token);
            return token;
        }).accept(token);
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
            .orElseGet(() -> {
                WebUtils.removeSimpleMultifactorAuthenticationToken(requestContext);
                val service = WebUtils.getService(requestContext);
                val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
                val token = mfaFactory.create(service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
                LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
                return token;
            });
    }
}
