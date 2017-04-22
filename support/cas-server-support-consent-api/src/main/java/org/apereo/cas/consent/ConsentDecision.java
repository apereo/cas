package org.apereo.cas.consent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

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

    private Set<String> attributeNames;
    private Set<String> attributeValues;

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

    public Set<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(final Set<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public Set<String> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(final Set<String> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
