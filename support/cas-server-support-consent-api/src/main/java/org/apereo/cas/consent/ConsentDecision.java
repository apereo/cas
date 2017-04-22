package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link ConsentDecision}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConsentDecision {
    private long id;

    private String principal;
    private String service;

    private LocalDateTime date;

    private long reminder = 14;
    private ChronoUnit reminderTimeUnit = ChronoUnit.DAYS;

    private String attributeNames;
    private String attributeValues;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    public long getReminder() {
        return reminder;
    }

    public void setReminder(final long reminder) {
        this.reminder = reminder;
    }

    public ChronoUnit getReminderTimeUnit() {
        return reminderTimeUnit;
    }

    public void setReminderTimeUnit(final ChronoUnit reminderTimeUnit) {
        this.reminderTimeUnit = reminderTimeUnit;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(final String principal) {
        this.principal = principal;
    }

    public String getService() {
        return service;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public String getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(final String attributeNames) {
        this.attributeNames = attributeNames;
    }

    public String getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(final String attributeValues) {
        this.attributeValues = attributeValues;
    }

    /**
     * Build consent decision consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @return the consent decision
     */
    public static ConsentDecision buildConsentDecision(final Service service,
                                                       final RegisteredService registeredService,
                                                       final Authentication authentication) {
        final ConsentDecision consent = new ConsentDecision();
        consent.setPrincipal(authentication.getPrincipal().getId());
        consent.setService(service.getId());

        final Map<String, Object> attributes =
                registeredService.getAttributeReleasePolicy().getAttributes(authentication.getPrincipal(), registeredService);

        final String names = DigestUtils.sha512(attributes.keySet().stream().collect(Collectors.joining("|")));
        consent.setAttributeNames(names);

        final String values = DigestUtils.sha512(attributes.values().stream()
                .map(CollectionUtils::toCollection)
                .map(c -> {
                    final String value = c.stream().map(Object::toString).collect(Collectors.joining());
                    return value;
                })
                .collect(Collectors.joining("|")));
        consent.setAttributeValues(values);

        consent.setDate(LocalDateTime.now());
        return consent;
    }
}
