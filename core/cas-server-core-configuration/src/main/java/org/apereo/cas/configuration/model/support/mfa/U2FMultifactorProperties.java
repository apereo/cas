package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class U2FMultifactorProperties extends BaseMultifactorProvider {
    private static final long serialVersionUID = 6151350313777066398L;

    /**
     * Store device registration records inside a JDBC resource.
     */
    private Jpa jpa = new Jpa();

    /**
     * Expire and forget device registration requests after this period.
     */
    private long expireRegistrations = 30;
    /**
     * Device registration requests expiration time unit.
     */
    private TimeUnit expireRegistrationsTimeUnit = TimeUnit.SECONDS;

    /**
     * Expire and forget device registration records after this period.
     */
    private long expireDevices = 30;
    /**
     * Device registration record expiration time unit.
     */
    private TimeUnit expireDevicesTimeUnit = TimeUnit.DAYS;

    /**
     * Store device registration records inside a static JSON resource.
     */
    private Json json = new Json();
    /**
     * Clean up expired records via a background cleaner process.
     */
    private Cleaner cleaner = new Cleaner();

    public U2FMultifactorProperties() {
        setId("mfa-u2f");
    }

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public long getExpireRegistrations() {
        return expireRegistrations;
    }

    public void setExpireRegistrations(final long expireRegistrations) {
        this.expireRegistrations = expireRegistrations;
    }

    public TimeUnit getExpireRegistrationsTimeUnit() {
        return expireRegistrationsTimeUnit;
    }

    public void setExpireRegistrationsTimeUnit(final TimeUnit expireRegistrationsTimeUnit) {
        this.expireRegistrationsTimeUnit = expireRegistrationsTimeUnit;
    }

    public long getExpireDevices() {
        return expireDevices;
    }

    public void setExpireDevices(final long expireDevices) {
        this.expireDevices = expireDevices;
    }

    public TimeUnit getExpireDevicesTimeUnit() {
        return expireDevicesTimeUnit;
    }

    public void setExpireDevicesTimeUnit(final TimeUnit expireDevicesTimeUnit) {
        this.expireDevicesTimeUnit = expireDevicesTimeUnit;
    }

    public static class Json extends AbstractConfigProperties {
        private static final long serialVersionUID = -6883660787308509919L;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = -4334840263678287815L;
    }

    public static class Cleaner implements Serializable {
        private static final long serialVersionUID = 8459671958275130605L;

        @NestedConfigurationProperty
        private SchedulingProperties schedule = new SchedulingProperties();

        public Cleaner() {
            schedule.setEnabled(true);
            schedule.setStartDelay("PT10S");
            schedule.setRepeatInterval("PT1M");
        }

        public SchedulingProperties getSchedule() {
            return schedule;
        }

        public void setSchedule(final SchedulingProperties schedule) {
            this.schedule = schedule;
        }
    }
}

