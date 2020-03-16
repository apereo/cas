package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link AuditJdbcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-audit-jdbc")
@Getter
@Setter
@Accessors(chain = true)
public class AuditJdbcProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 4227475246873515918L;

    /**
     * Execute the recording of audit records in async manner.
     * This setting must almost always be set to true.
     */
    private boolean asynchronous = true;

    /**
     * Indicates how long audit records should be kept in the database.
     * This is used by the clean-up criteria to clean up after stale audit records.
     */
    private int maxAgeDays = 180;

    /**
     * Allows one to trim the audit data by the specified length.
     * A negative value disables the trimming process where the audit
     * functionality no longer substrings the audit record.
     */
    private int columnLength = 100;

    /**
     * Scheduler settings to indicate how often the cleaner is reloaded.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();
}
