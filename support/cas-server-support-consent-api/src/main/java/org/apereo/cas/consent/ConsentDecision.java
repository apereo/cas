package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link ConsentDecision}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Table(name = "ConsentDecision")
public class ConsentDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String principal;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String service;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private Long reminder = 14L;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private TimeUnit reminderTimeUnit = TimeUnit.DAYS;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String attributeNames;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
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

    public TimeUnit getReminderTimeUnit() {
        return reminderTimeUnit;
    }

    public void setReminderTimeUnit(final TimeUnit reminderTimeUnit) {
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
                registeredService.getAttributeReleasePolicy().getAttributes(authentication.getPrincipal(),
                        service, registeredService);

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
