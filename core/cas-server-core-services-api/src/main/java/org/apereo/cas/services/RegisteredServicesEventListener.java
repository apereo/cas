package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.io.CommunicationsManager;
import org.springframework.context.event.EventListener;

/**
 * This is {@link RegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
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
        final var registeredService = event.getRegisteredService();
        final var contacts = registeredService.getContacts();

        final var mail = casProperties.getServiceRegistry().getMail();
        final var sms = casProperties.getServiceRegistry().getSms();

        final var serviceName = StringUtils.defaultIfBlank(registeredService.getName(), registeredService.getServiceId());
        if (communicationsManager.isMailSenderDefined()) {
            final var message = String.format(mail.getText(), serviceName);
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
            final var message = String.format(sms.getText(), serviceName);
            contacts
                    .stream()
                    .filter(c -> StringUtils.isNotBlank(c.getPhone()))
                    .forEach(c -> communicationsManager.sms(sms.getFrom(), c.getPhone(), message));
        }

        servicesManager.load();
    }

}
