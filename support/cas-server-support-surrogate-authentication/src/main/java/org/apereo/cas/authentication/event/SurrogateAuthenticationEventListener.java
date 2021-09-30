package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.spring.CasEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticationEventListener implements CasEventListener {
    private final CommunicationsManager communicationsManager;
    private final CasConfigurationProperties casProperties;

    /**
     * Handle failure event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleSurrogateAuthenticationFailureEvent(final CasSurrogateAuthenticationFailureEvent event) {
        notify(event.getPrincipal(), event);
    }

    /**
     * Handle success event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleSurrogateAuthenticationSuccessEvent(final CasSurrogateAuthenticationSuccessfulEvent event) {
        notify(event.getPrincipal(), event);
    }

    /**
     * Notify.
     *
     * @param principal the principal
     * @param event     the event
     */
    protected void notify(final Principal principal, final AbstractCasEvent event) {
        val eventDetails = event.toString();
        if (communicationsManager.isSmsSenderDefined()) {
            val sms = casProperties.getAuthn().getSurrogate().getSms();
            val smsAttribute = sms.getAttributeName();
            val to = principal.getAttributes().get(smsAttribute);
            if (to != null && StringUtils.isNotBlank(sms.getText())) {
                val text = sms.getFormattedText("\n\n".concat(eventDetails));
                this.communicationsManager.sms(sms.getFrom(), to.toString(), text);
            } else {
                LOGGER.trace("The principal has no [{}] attribute, cannot send SMS notification", smsAttribute);
            }
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication SMS messages given no settings are defined to account for servers, etc");
        }
        if (communicationsManager.isMailSenderDefined()) {
            val mail = casProperties.getAuthn().getSurrogate().getMail();
            val emailAttribute = mail.getAttributeName();
            val to = principal.getAttributes().get(emailAttribute);
            if (to != null) {
                val body = EmailMessageBodyBuilder.builder().properties(mail)
                    .parameters(Map.of("event", eventDetails)).build().produce();
                this.communicationsManager.email(mail, to.toString(), body);
            } else {
                LOGGER.trace("The principal has no [{}] attribute, cannot send email notification", emailAttribute);
            }
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication email messages given no settings are defined to account for servers, etc");
        }
    }
}
