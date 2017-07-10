package org.apereo.cas.configuration.model.support.consent;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link ConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConsentProperties {
    private int reminder = 30;
    private TimeUnit reminderTimeUnit = TimeUnit.DAYS;

    private Rest rest = new Rest();
    private Jpa jpa = new Jpa();
    private Json json = new Json();
    
    private String encryptionKey = StringUtils.EMPTY;
    private String signingKey = StringUtils.EMPTY;
    private boolean cipherEnabled = true;

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

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

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public void setSigningKey(final String signingKey) {
        this.signingKey = signingKey;
    }

    public boolean isCipherEnabled() {
        return cipherEnabled;
    }

    public void setCipherEnabled(final boolean cipherEnabled) {
        this.cipherEnabled = cipherEnabled;
    }

    public static class Json extends AbstractConfigProperties {
        private static final long serialVersionUID = 7079027843747126083L;
    }
    
    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 1646689616653363554L;
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = -6909617495470495341L;

        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
