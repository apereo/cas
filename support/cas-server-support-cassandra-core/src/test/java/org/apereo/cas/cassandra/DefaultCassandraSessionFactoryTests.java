package org.apereo.cas.cassandra;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCassandraSessionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Cassandra")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 9042)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreWebAutoConfiguration.class, properties = {
    "cas.authn.cassandra.table-name=users_table",
    "cas.authn.cassandra.local-dc=datacenter1",
    "cas.authn.cassandra.username-attribute=user_attr",
    "cas.authn.cassandra.password-attribute=pwd_attr",
    "cas.authn.cassandra.keyspace=cas",
    "cas.authn.cassandra.ssl-protocols=TLSv1.2",
    "cas.http-client.host-name-verifier=none"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultCassandraSessionFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    private CasSSLContext casSslContext;

    @Test
    void verifyOperation() {
        val cassandra = casProperties.getAuthn().getCassandra();
        val factory = new DefaultCassandraSessionFactory(cassandra, casSslContext.getSslContext());
        assertNotNull(factory.getCassandraTemplate());
        assertNotNull(factory.getCqlTemplate());
        assertNotNull(factory.getSession());
        factory.close();
    }
}
