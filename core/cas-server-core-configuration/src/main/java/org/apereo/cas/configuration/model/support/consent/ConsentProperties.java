package org.apereo.cas.configuration.model.support.consent;

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
}
