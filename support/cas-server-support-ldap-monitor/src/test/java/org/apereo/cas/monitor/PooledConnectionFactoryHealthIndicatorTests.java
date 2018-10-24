package org.apereo.cas.monitor;

import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.monitor.config.LdapMonitorConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

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
})
@TestPropertySource(properties = {
    "cas.monitor.ldap.ldapUrl=ldap://localhost:10389",
    "cas.monitor.ldap.useSsl=false"
})
@Category(LdapCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class PooledConnectionFactoryHealthIndicatorTests {
    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();
    
    @Autowired
    @Qualifier("pooledLdapConnectionFactoryHealthIndicator")
    private HealthIndicator monitor;

    @Test
    public void verifyObserve() {
        assertEquals(Status.UP, monitor.health().getStatus());
    }
}
