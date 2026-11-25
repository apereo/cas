package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasLdapMonitorAutoConfiguration;
import org.apereo.cas.configuration.model.core.monitor.LdapMonitorProperties;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link PooledLdapConnectionFactoryHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasLdapMonitorAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
},
    properties = {
        "cas.monitor.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.monitor.ldap[0].name=LDAP"
    })
@Tag("Ldap")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 10389)
class PooledLdapConnectionFactoryHealthIndicatorTests {
    @Autowired
    @Qualifier("pooledLdapConnectionFactoryHealthIndicator")
    private CompositeHealthContributor monitor;

    @Autowired
    @Qualifier("pooledLdapConnectionFactoryHealthIndicatorListFactoryBean")
    private ListFactoryBean pooledLdapConnectionFactoryHealthIndicatorListFactoryBean;

    @Autowired
    @Qualifier("ldapClusterTopologyManager")
    private ClusterTopologyManager ldapClusterTopologyManager;

    @Test
    @SuppressWarnings("unchecked")
    void verifyLdapClusterTopology() throws Throwable {
        val pooled = new LdapMonitorProperties();
        pooled.setLdapUrl("ldap://localhost:10389");
        pooled.setBindDn("cn=Directory Manager");
        pooled.setBindCredential("password");
        pooled.setName("topology-pool");
        val factory = LdapUtils.newLdaptivePooledConnectionFactory(pooled);

        val direct = new LdapMonitorProperties();
        direct.setLdapUrl("ldap://localhost:10389");
        direct.setBindDn("cn=Directory Manager");
        direct.setBindCredential("password");
        direct.setName("topology-direct");
        val directFactory = LdapUtils.newLdaptiveDefaultConnectionFactory(direct);

        val unreachable = new LdapMonitorProperties();
        unreachable.setLdapUrl("ldap://localhost:10399");
        unreachable.setName("topology-unreachable");
        val unreachableFactory = LdapUtils.newLdaptiveDefaultConnectionFactory(unreachable);

        try (val connection = factory.getConnection()) {
            assertTrue(connection.isOpen());
            assertEquals("LDAP", ldapClusterTopologyManager.getName());
            val manager = new LdapClusterTopologyManager(Duration.ofSeconds(5), Duration.ofSeconds(30));
            val members = manager.discoverMembers();

            val local = members.stream().filter(member -> "ldap://localhost:10389".equals(member.getId())).findFirst().orElseThrow();
            assertEquals("LDAP", local.getOwner());
            assertTrue(local.isStatus());
            val usedBy = (List<String>) local.getAttributes().get("usedBy");
            assertTrue(usedBy.contains("LDAP Monitor (topology-pool)"));
            assertTrue(usedBy.contains("LDAP Monitor (topology-direct)"));
            val connections = (Map<String, String>) local.getAttributes().get("connections");
            assertTrue(connections.get("LDAP Monitor (topology-pool)").contains("1 in use"));
            assertFalse(connections.containsKey("LDAP Monitor (topology-direct)"));
            assertNotNull(local.getAttributes().get("vendor"));
            assertEquals(1, members.stream().filter(member -> "ldap://localhost:10389".equals(member.getId())).count());

            val failed = members.stream().filter(member -> "ldap://localhost:10399".equals(member.getId())).findFirst().orElseThrow();
            assertFalse(failed.isStatus());
            assertNotNull(failed.getAttributes().get("lastFailure"));
            assertEquals(List.of("LDAP Monitor (topology-unreachable)"), failed.getAttributes().get("usedBy"));

            assertSame(members, manager.discoverMembers());
        } finally {
            factory.close();
            directFactory.close();
            unreachableFactory.close();
        }
        assertEquals("Headquarters", LdapClusterTopologyManager.siteOf(
            "CN=DC1,CN=Servers,CN=Headquarters,CN=Sites,CN=Configuration,DC=example,DC=org"));
        assertNull(LdapClusterTopologyManager.siteOf("cn=casuser,ou=people,dc=example,dc=org"));
        assertEquals("001", LdapClusterTopologyManager.providerOf("20261118120000.123456Z#000000#001#000000"));
        val bind = new IllegalStateException("Connection initializer org.ldaptive.BindConnectionInitializer@1::bindDn=cn=Directory Manager "
            + "returned response: org.ldaptive.BindResponse@2::messageID=1, controls=[], resultCode=INVALID_CREDENTIALS, matchedDN=, "
            + "diagnosticMessage=, referralURLs=[] for URL [org.ldaptive.LdapURL@3::url=ldap://localhost:1389, inetAddress=null]");
        assertEquals("The server refused the bind: invalid credentials", LdapClusterTopologyManager.reasonOf(bind));
        assertEquals("Connection refused", LdapClusterTopologyManager.reasonOf(new IllegalStateException("Could not connect",
            new IllegalArgumentException("Connection refused"))));
    }

    @Test
    void verifyObserve() throws Throwable {
        val results = monitor.stream()
            .map(it -> (HealthIndicator) it.contributor())
            .map(it -> it.health().getStatus()).toList();
        assertFalse(results.isEmpty());
        assertEquals(Status.UP, results.getFirst());
        pooledLdapConnectionFactoryHealthIndicatorListFactoryBean.destroy();
    }
}
