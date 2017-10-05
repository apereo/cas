package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.io.CommunicationsManager;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * This is {@link RegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServicesEventListener {
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;
    private final CommunicationsManager communicationsManager;

    public RegisteredServicesEventListener(final ServicesManager servicesManager,
                                           final CasConfigurationProperties casProperties, 
                                           final CommunicationsManager communicationsManager) {
        this.servicesManager = servicesManager;
        this.casProperties = casProperties;
        this.communicationsManager = communicationsManager;
    }

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
        final RegisteredService registeredService = event.getRegisteredService();
        final List<RegisteredServiceContact> contacts = registeredService.getContacts();

        final EmailProperties mail = casProperties.getServiceRegistry().getMail();
        final SmsProperties sms = casProperties.getServiceRegistry().getSms();

        final String serviceName = StringUtils.defaultIfBlank(registeredService.getName(), registeredService.getServiceId());
        if (communicationsManager.isMailSenderDefined()) {
            final String message = String.format(mail.getText(), serviceName);
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
            final String message = String.format(sms.getText(), serviceName);
            contacts
                    .stream()
                    .filter(c -> StringUtils.isNotBlank(c.getPhone()))
                    .forEach(c -> communicationsManager.sms(sms.getFrom(), c.getPhone(), message));
        }

        servicesManager.load();
    }

}
