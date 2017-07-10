package org.apereo.cas.consent;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
    private long id = -1;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String principal;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String service;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private ConsentOptions options;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private Long reminder = 14L;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private TimeUnit reminderTimeUnit = TimeUnit.DAYS;

    @Column(length = 4096, updatable = true, insertable = true, nullable = false)
    private String attributeNames;

    @Column(length = 4096, updatable = true, insertable = true, nullable = false)
    private String attributeValues;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(final LocalDateTime date) {
        this.date = date;
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

    public ConsentOptions getOptions() {
        return options;
    }

    public void setOptions(final ConsentOptions options) {
        this.options = options;
    }

    public void setReminder(final Long reminder) {
        this.reminder = reminder;
    }

    public Long getReminder() {
        return reminder;
    }

    
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("principal", principal)
                .append("service", service)
                .append("date", date)
                .append("options", options)
                .append("reminder", reminder)
                .append("reminderTimeUnit", reminderTimeUnit)
                .toString();
    }
}
