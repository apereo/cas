package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.monitor.config.LdapMonitorConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.stream.Collectors;

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
        "cas.monitor.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.monitor.ldap[0].name=LDAP"
    })
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
public class PooledLdapConnectionFactoryHealthIndicatorTests {
    @Autowired
    @Qualifier("pooledLdapConnectionFactoryHealthIndicator")
    private CompositeHealthContributor monitor;

    @Test
    public void verifyObserve() {
        val results = monitor.stream()
            .map(it -> HealthIndicator.class.cast(it.getContributor()))
            .map(it -> it.health().getStatus())
            .collect(Collectors.toList());
        assertFalse(results.isEmpty());
        assertEquals(Status.UP, results.get(0));
    }
}
