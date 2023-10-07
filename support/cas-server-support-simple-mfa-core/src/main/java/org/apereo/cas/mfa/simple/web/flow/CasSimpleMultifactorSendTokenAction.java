package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
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

    protected final CommunicationsManager communicationsManager;

    protected final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    protected final CasSimpleMultifactorAuthenticationProperties properties;

    protected final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy;

    protected final BucketConsumer bucketConsumer;

    protected boolean isNotificationSent(final Principal principal, final Ticket token) {
        return communicationsManager.isNotificationSenderDefined()
            && communicationsManager.notify(principal, "Apereo CAS Token", String.format("Token: %s", token.getId()));
    }

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
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        val token = getOrCreateToken(requestContext, principal);
        LOGGER.debug("Using token [{}] created at [{}]", token.getId(), token.getCreationTime());

        val strategy = tokenCommunicationStrategy.determineStrategy(token);
        val smsSent = strategy.contains(TokenSharingStrategyOptions.SMS)
            && CasSimpleMultifactorSendSms.of(communicationsManager, properties).send(principal, token, requestContext);

        val phoneCallSent = strategy.contains(TokenSharingStrategyOptions.PHONE)
            && CasSimpleMultifactorMakePhoneCall.of(communicationsManager, properties).call(principal, token, requestContext);

        return FunctionUtils.doUnchecked(() -> {
            var emailSent = false;
            if (strategy.contains(TokenSharingStrategyOptions.EMAIL)) {
                val cmd = CasSimpleMultifactorSendEmail.of(communicationsManager, properties);
                val recipients = cmd.getEmailMessageRecipients(principal);
                if (recipients.size() > 1) {
                    val selectedEmailRecipients = findSelectedEmailRecipients(requestContext, principal);
                    LOGGER.debug("Selected email recipients are [{}]", selectedEmailRecipients);
                    if (selectedEmailRecipients.isEmpty()) {
                        return buildSelectEmailRecipientEvent(requestContext, principal, recipients);
                    }
                    emailSent = cmd.send(principal, token, selectedEmailRecipients, requestContext).isAnyEmailSent();
                } else {
                    emailSent = cmd.send(principal, token, requestContext).isAnyEmailSent();
                }
            }

            val notificationSent = strategy.contains(TokenSharingStrategyOptions.NOTIFICATION) && isNotificationSent(principal, token);
            if (smsSent || emailSent || notificationSent || phoneCallSent) {
                LOGGER.debug("Successfully submitted token via strategy option [{}] to [{}]", strategy, principal.getId());
                storeToken(requestContext, token);
                return buildSuccessEvent(token);
            }
            LOGGER.error("Communication strategies failed to submit token [{}] to user", token.getId());
            return error();
        });
    }

    protected List<String> findSelectedEmailRecipients(final RequestContext requestContext, final Principal principal) {
        val parameters = requestContext.getRequestParameters().asMap();
        val emailRecipients = (Map<String, CandidateEmailAddress>) requestContext.getFlowScope().get("emailRecipients", Map.class);

        if (emailRecipients == null || emailRecipients.isEmpty()) {
            LOGGER.debug("No selected email recipients are found in the request context");
            return List.of();
        }
        
        return parameters.keySet()
            .stream()
            .filter(emailRecipients::containsKey)
            .map(entry -> emailRecipients.get(entry).email())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Event buildSelectEmailRecipientEvent(final RequestContext requestContext, final Principal principal,
                                                 final List<String> recipients) {
        val emailDomainPattern = Pattern.compile(".{4}@.*");
        val validAddresses = recipients
            .stream()
            .map(address -> {
                val hash = DigestUtils.sha512(address);
                val obfuscated = emailDomainPattern.matcher(address).replaceAll("******@******");
                return Pair.of(hash, new CandidateEmailAddress(hash, address, obfuscated));
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        val attributes = new LocalAttributeMap<Object>("emailRecipients", validAddresses);
        WebUtils.putPrincipal(requestContext, principal);
        requestContext.getFlowScope().put("emailRecipients", validAddresses);
        LOGGER.debug("Multiple emails found for [{}]: [{}]", principal.getId(), validAddresses);
        return new EventFactorySupport().event(this, "selectEmails", attributes);
    }

    protected Event buildSuccessEvent(final CasSimpleMultifactorAuthenticationTicket token) {
        val attributes = new LocalAttributeMap<Object>("token", token.getId());
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS, attributes);
    }

    protected void storeToken(final RequestContext requestContext, final CasSimpleMultifactorAuthenticationTicket token) throws Throwable {
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

    private String getThrottledRequestKeyFor(final Authentication authentication,
                                             final RequestContext requestContext) {
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        return principal.getId();
    }

    public record CandidateEmailAddress(String hash, String email, String obfuscated) implements Serializable {}
}
