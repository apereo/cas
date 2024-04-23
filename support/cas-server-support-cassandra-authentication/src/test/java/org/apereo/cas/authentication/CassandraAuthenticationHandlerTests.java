package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CassandraAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CassandraAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CassandraAuthenticationAutoConfiguration.class
}, properties = {
    "cas.authn.cassandra.table-name=users_table",
    "cas.authn.cassandra.local-dc=datacenter1",
    "cas.authn.cassandra.username-attribute=user_attr",
    "cas.authn.cassandra.password-attribute=pwd_attr",
    "cas.authn.cassandra.keyspace=cas",
    "cas.authn.cassandra.username=casuser",
    "cas.authn.cassandra.password=password",
    "cas.authn.cassandra.ssl-protocols=TLSv1.2",
    "cas.http-client.host-name-verifier=none"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Cassandra")
@EnabledIfListeningOnPort(port = 9042)
class CassandraAuthenticationHandlerTests {
    @Autowired
    @Qualifier("cassandraAuthenticationHandler")
    private AuthenticationHandler cassandraAuthenticationHandler;

    @Test
    void verifyUserNotFound() throws Throwable {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("baduser", "Mellon");
        assertThrows(AccountNotFoundException.class, () -> cassandraAuthenticationHandler.authenticate(c, mock(Service.class)));
    }

    @Test
    void verifyUserBadPassword() throws Throwable {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "bad");
        assertThrows(FailedLoginException.class, () -> cassandraAuthenticationHandler.authenticate(c, mock(Service.class)));
    }

    @Test
    void verifyUser() throws Throwable {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = cassandraAuthenticationHandler.authenticate(c, mock(Service.class));
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
    }
}
