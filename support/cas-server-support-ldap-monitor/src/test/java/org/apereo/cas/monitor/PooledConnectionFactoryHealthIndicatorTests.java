package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.monitor.config.LdapMonitorConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link PooledLdapConnectionFactoryHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@SpringBootTest(classes = {
    LdapMonitorConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.monitor.ldap.ldapUrl=ldap://localhost:10389",
        "cas.monitor.ldap.useSsl=false"
    })
@Tag("Ldap")
@EnabledIfContinuousIntegration
public class PooledConnectionFactoryHealthIndicatorTests {
    @Autowired
    @Qualifier("pooledLdapConnectionFactoryHealthIndicator")
    private HealthIndicator monitor;

    @Test
    public void verifyObserve() {
        assertEquals(Status.UP, monitor.health().getStatus());
    }
}
