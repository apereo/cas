package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JdbcMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-jdbc-monitor")
@Getter
@Setter
@Accessors(chain = true)
public class JdbcMonitorProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = -7139788158851782673L;

    /**
     * The query to execute against the database to monitor status.
     */
    private String validationQuery = "SELECT 1";

    /**
     * When monitoring the JDBC connection pool, indicates the amount of time the operation must wait
     * before it times outs and considers the pool in bad shape.
     */
    @DurationCapable
    private String maxWait = "PT5S";
}
