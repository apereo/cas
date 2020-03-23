package org.apereo.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link RegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
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
    @Async
    public void handleRefreshEvent(final CasRegisteredServicesRefreshEvent event) {
        servicesManager.load();
    }

    /**
     * Handle registered service expired event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleRegisteredServiceExpiredEvent(final CasRegisteredServiceExpiredEvent event) {
        val registeredService = event.getRegisteredService();
        val contacts = registeredService.getContacts();
        val serviceRegistry = casProperties.getServiceRegistry();
        val serviceName = StringUtils.defaultIfBlank(registeredService.getName(), registeredService.getServiceId());
        if (contacts == null || contacts.isEmpty()) {
            LOGGER.debug("No contacts are defined to be notified for policy changes to service [{}]", serviceName);
            return;
        }

        val logMessage = String.format("Sending notification to [{}] as service [{}] is %s from registry", event.isDeleted() ? "deleted" : "expired");
        LOGGER.info(logMessage, contacts, serviceName);

        communicationsManager.validate();
        if (communicationsManager.isMailSenderDefined()) {
            val mail = serviceRegistry.getMail();
            val message = mail.getFormattedBody(serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getEmail()))
                .forEach(c -> communicationsManager.email(mail, c.getEmail(), message));
        }
        if (communicationsManager.isSmsSenderDefined()) {
            val sms = serviceRegistry.getSms();
            val message = sms.getFormattedText(serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getPhone()))
                .forEach(c -> communicationsManager.sms(sms.getFrom(), c.getPhone(), message));
        }
    }
}
