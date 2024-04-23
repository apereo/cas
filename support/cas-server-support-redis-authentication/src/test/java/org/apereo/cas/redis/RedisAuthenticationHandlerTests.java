package org.apereo.cas.redis;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRedisAuthenticationAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RedisAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasRedisAuthenticationAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.redis.host=localhost",
    "cas.authn.redis.port=6379",
    "cas.authn.redis.password-encoder.type=DEFAULT",
    "cas.authn.redis.password-encoder.encoding-algorithm=SHA-512"
})
@EnableScheduling
@Tag("Redis")
@EnabledIfListeningOnPort(port = 6379)
class RedisAuthenticationHandlerTests {

    @Autowired
    @Qualifier("redisAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("authenticationRedisTemplate")
    private CasRedisTemplate authenticationRedisTemplate;

    @BeforeEach
    public void initialize() {
        createUser("casuser", RedisUserAccount.AccountStatus.OK);
        createUser("casdisabled", RedisUserAccount.AccountStatus.DISABLED);
        createUser("caslocked", RedisUserAccount.AccountStatus.LOCKED);
        createUser("casexpired", RedisUserAccount.AccountStatus.EXPIRED);
        createUser("caschangepsw", RedisUserAccount.AccountStatus.MUST_CHANGE_PASSWORD);
    }

    private void createUser(final String uid, final RedisUserAccount.AccountStatus status) {
        val acct = new RedisUserAccount(uid, DigestUtils.sha512("caspassword"),
            CollectionUtils.wrap("name", List.of("CAS"), "group", List.of("sso")), status);
        authenticationRedisTemplate.opsForValue().set(acct.getUsername(), acct);
    }

    @Test
    void verifySuccessful() throws Throwable {
        val result = authenticationHandler.authenticate(new UsernamePasswordCredential("casuser", "caspassword"), mock(Service.class));
        assertNotNull(result);
        val principal = result.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getAttributes());
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("group"));
    }

    @Test
    void verifyNotFound() throws Throwable {
        assertThrows(AccountNotFoundException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("123456", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyInvalid() throws Throwable {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casuser", "badpassword"), mock(Service.class)));
    }

    @Test
    void verifyExpired() throws Throwable {
        assertThrows(AccountExpiredException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casexpired", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyLocked() throws Throwable {
        assertThrows(AccountLockedException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caslocked", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyChangePsw() throws Throwable {
        assertThrows(AccountPasswordMustChangeException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caschangepsw", "caspassword"), mock(Service.class)));
    }
}
