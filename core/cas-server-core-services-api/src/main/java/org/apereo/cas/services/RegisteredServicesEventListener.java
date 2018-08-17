package org.apereo.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;

/**
 * This is {@link RegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class RegisteredServicesEventListener {
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;
    private final CommunicationsManager communicationsManager;

    /**
     * Handle services manager refresh event.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final CasRegisteredServicesRefreshEvent event) {
        servicesManager.load();
    }

    /**
     * Handle registered service expired event.
     *
     * @param event the event
     */
    @EventListener
    public void handleRegisteredServiceExpiredEvent(final CasRegisteredServiceExpiredEvent event) {
        val registeredService = event.getRegisteredService();
        val contacts = registeredService.getContacts();

        val mail = casProperties.getServiceRegistry().getMail();
        val sms = casProperties.getServiceRegistry().getSms();

        val serviceName = StringUtils.defaultIfBlank(registeredService.getName(), registeredService.getServiceId());
        if (communicationsManager.isMailSenderDefined()) {
            val message = String.format(mail.getText(), serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getEmail()))
                .forEach(c -> communicationsManager.email(message,
                    mail.getFrom(),
                    mail.getSubject(),
                    c.getEmail(),
                    mail.getCc(),
                    mail.getBcc()));
        }
        if (communicationsManager.isSmsSenderDefined()) {
            val message = String.format(sms.getText(), serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getPhone()))
                .forEach(c -> communicationsManager.sms(sms.getFrom(), c.getPhone(), message));
        }

        servicesManager.load();
    }

}
