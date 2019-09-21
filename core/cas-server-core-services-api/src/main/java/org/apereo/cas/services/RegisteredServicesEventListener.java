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

        val serviceRegistry = casProperties.getServiceRegistry();
        val mail = serviceRegistry.getMail();
        val sms = serviceRegistry.getSms();

        val serviceName = StringUtils.defaultIfBlank(registeredService.getName(), registeredService.getServiceId());
        if (event.isDeleted()) {
            LOGGER.info("Sending notification to [{}] as registered service [{}] is deleted from service registry", contacts, serviceName);
        } else {
            LOGGER.info("Sending notification to [{}] as registered service [{}] is expired in service registry", contacts, serviceName);
        }

        communicationsManager.validate();
        if (communicationsManager.isMailSenderDefined()) {
            val message = mail.getFormattedBody(serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getEmail()))
                .forEach(c -> communicationsManager.email(mail, c.getEmail(), message));
        }
        if (communicationsManager.isSmsSenderDefined()) {
            val message = sms.getFormattedText(serviceName);
            contacts
                .stream()
                .filter(c -> StringUtils.isNotBlank(c.getPhone()))
                .forEach(c -> communicationsManager.sms(sms.getFrom(), c.getPhone(), message));
        }
    }
}
