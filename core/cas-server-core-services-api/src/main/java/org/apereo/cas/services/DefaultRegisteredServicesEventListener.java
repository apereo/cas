package org.apereo.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultRegisteredServicesEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultRegisteredServicesEventListener implements RegisteredServicesEventListener {
    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    private final CommunicationsManager communicationsManager;

    @Override
    public void handleRefreshEvent(final CasRegisteredServicesRefreshEvent event) {
        servicesManager.load();
    }

    @Override
    public void handleEnvironmentChangeEvent(final EnvironmentChangeEvent event) {
        servicesManager.load();
    }

    @Override
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
            val body = EmailMessageBodyBuilder.builder()
                .properties(mail)
                .parameters(Map.of("service", serviceName))
                .build().get();

            contacts
                .stream()
                .filter(contact -> StringUtils.isNotBlank(contact.getEmail()))
                .forEach(contact -> {
                    val emailRequest = EmailMessageRequest.builder()
                        .emailProperties(mail)
                        .to(List.of(contact.getEmail()))
                        .body(body).build();
                    communicationsManager.email(emailRequest);
                });
        }
        if (communicationsManager.isSmsSenderDefined()) {
            val sms = serviceRegistry.getSms();
            val message = SmsBodyBuilder.builder().properties(sms).parameters(Map.of("service", serviceName)).build().get();
            contacts
                .stream()
                .filter(contact -> StringUtils.isNotBlank(contact.getPhone()))
                .forEach(Unchecked.consumer(contact -> {
                    val smsRequest = SmsRequest.builder().from(sms.getFrom())
                        .to(contact.getPhone()).text(message).build();
                    communicationsManager.sms(smsRequest);
                }));
        }
    }
}
