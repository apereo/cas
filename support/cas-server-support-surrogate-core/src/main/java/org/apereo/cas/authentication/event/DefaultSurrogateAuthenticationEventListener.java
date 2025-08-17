package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultSurrogateAuthenticationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultSurrogateAuthenticationEventListener implements SurrogateAuthenticationEventListener {
    private final CommunicationsManager communicationsManager;

    private final CasConfigurationProperties casProperties;

    private final TenantExtractor tenantExtractor;
    
    @Override
    public void handleSurrogateAuthenticationFailureEvent(final CasSurrogateAuthenticationFailureEvent event) throws Throwable {
        notify(event.getPrincipal(), event);
    }

    @Override
    public void handleSurrogateAuthenticationSuccessEvent(final CasSurrogateAuthenticationSuccessfulEvent event) throws Throwable {
        notify(event.getPrincipal(), event);
    }

    protected void notify(final Principal principal, final AbstractCasEvent event) throws Throwable {
        val eventDetails = event.toString();
        if (communicationsManager.isSmsSenderDefined()) {
            val sms = casProperties.getAuthn().getSurrogate().getSms();
            val text = SmsBodyBuilder.builder()
                .properties(sms)
                .parameters(Map.of("details", eventDetails))
                .build().
                get();

            val smsRequests = sms.getAttributeName()
                .stream()
                .map(attribute ->
                    SmsRequest.builder()
                        .principal(principal)
                        .attribute(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute))
                        .from(sms.getFrom())
                        .tenant(event.getClientInfo().getTenant())
                        .text(text)
                        .build())
                .collect(Collectors.<SmsRequest>toList());

            communicationsManager.sms(smsRequests);
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication SMS messages given no settings are defined to account for servers, etc");
        }
        if (communicationsManager.isMailSenderDefined()) {
            val emailAttributes = casProperties.getAuthn().getSurrogate().getMail().getAttributeName();
            emailAttributes.forEach(attribute -> sendEmail(principal, attribute, eventDetails, event.getClientInfo()));
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication email messages given no settings are defined to account for servers, etc");
        }
    }

    protected void sendEmail(final Principal principal, final String emailAttribute,
                             final String eventDetails, final ClientInfo clientInfo) {
        val mail = casProperties.getAuthn().getSurrogate().getMail();

        val resolvedAttribute = SpringExpressionLanguageValueResolver.getInstance().resolve(emailAttribute);
        val to = principal.getAttributes().get(resolvedAttribute);
        if (to != null) {
            CollectionUtils.firstElement(to).ifPresent(address -> {
                val body = EmailMessageBodyBuilder.builder()
                    .properties(mail)
                    .parameters(Map.of("event", eventDetails))
                    .build()
                    .get();

                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(mail)
                    .to(List.of(address.toString()))
                    .tenant(clientInfo.getTenant())
                    .body(body)
                    .build();
                communicationsManager.email(emailRequest);
            });
        } else {
            LOGGER.trace("The principal has no [{}] attribute, cannot send email notification", resolvedAttribute);
        }
    }
}
