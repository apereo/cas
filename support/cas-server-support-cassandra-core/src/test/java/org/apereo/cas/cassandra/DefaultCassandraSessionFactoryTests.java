package org.apereo.cas.cassandra;

/**
 * This is {@link org.apereo.cas.cassandra.DefaultCassandraSessionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("Cassandra")
@EnabledIfPortOpen(port = 9042)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
}, properties = {
    "cas.authn.cassandra.table-name=users_table",
    "cas.authn.cassandra.local-dc=datacenter1",
    "cas.authn.cassandra.username-attribute=user_attr",
    "cas.authn.cassandra.password-attribute=pwd_attr",
    "cas.authn.cassandra.keyspace=cas"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultCassandraSessionFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casSslContext")
    private CasSSLContext casSslContext;

    @Test
    public void verifyOperation() {
        val cassandra = casProperties.getAuthn().getCassandra();
        val factory = new DefaultCassandraSessionFactory(cassandra, casSslContext.getSslContext());
        assertNotNull(factory.getCassandraTemplate());
        assertNotNull(factory.getCqlTemplate());
        assertNotNull(factory.getSession());
        factory.close();
    }
}
