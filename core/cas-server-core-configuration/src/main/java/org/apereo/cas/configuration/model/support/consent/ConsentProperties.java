package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link ConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConsentProperties {
    private int reminder = 14;
    private TimeUnit reminderTimeUnit = TimeUnit.DAYS;

    private Jpa jpa = new Jpa();

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(final int reminder) {
        this.reminder = reminder;
    }

    public TimeUnit getReminderTimeUnit() {
        return reminderTimeUnit;
    }

    public void setReminderTimeUnit(final TimeUnit reminderTimeUnit) {
        this.reminderTimeUnit = reminderTimeUnit;
    }

    public static class Jpa extends AbstractJpaProperties {
    }
}
