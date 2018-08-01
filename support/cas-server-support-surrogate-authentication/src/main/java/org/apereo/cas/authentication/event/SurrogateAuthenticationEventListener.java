package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;

/**
 * This is {@link SurrogateAuthenticationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticationEventListener {
    private final CommunicationsManager communicationsManager;
    private final CasConfigurationProperties casProperties;

    /**
     * Handle failure event.
     *
     * @param event the event
     */
    @EventListener
    public void handleSurrogateAuthenticationFailureEvent(final CasSurrogateAuthenticationFailureEvent event) {
        notify(event.getPrincipal(), event);
    }

    /**
     * Handle success event.
     *
     * @param event the event
     */
    @EventListener
    public void handleSurrogateAuthenticationSuccessEvent(final CasSurrogateAuthenticationSuccessfulEvent event) {
        notify(event.getPrincipal(), event);
    }

    private void notify(final Principal principal, final AbstractCasEvent event) {
        val eventDetails = event.toString();
        if (communicationsManager.isSmsSenderDefined()) {
            val sms = casProperties.getAuthn().getSurrogate().getSms();
            val smsAttribute = sms.getAttributeName();
            val to = principal.getAttributes().get(smsAttribute);
            if (to != null) {
                val text = sms.getText().concat("\n").concat(eventDetails);
                this.communicationsManager.sms(sms.getFrom(), to.toString(), text);
            } else {
                LOGGER.trace("The principal has no {} attribute, cannot send SMS notification", smsAttribute);
            }
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication SMS messages given no settings are defined to account for servers, etc");
        }
        if (communicationsManager.isMailSenderDefined()) {
            val mail = casProperties.getAuthn().getSurrogate().getMail();
            val emailAttribute = mail.getAttributeName();
            val to = principal.getAttributes().get(emailAttribute);
            if (to != null) {
                val text = mail.getText().concat("\n").concat(eventDetails);
                this.communicationsManager.email(text, mail.getFrom(), mail.getSubject(), to.toString(), mail.getCc(), mail.getBcc());
            } else {
                LOGGER.trace("The principal has no {} attribute, cannot send email notification", emailAttribute);
            }
        } else {
            LOGGER.trace("CAS is unable to send surrogate-authentication email messages given no settings are defined to account for servers, etc");
        }
    }
}
