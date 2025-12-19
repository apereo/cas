package org.apereo.cas.redis;

import module java.base;
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
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RedisAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
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
    CasCoreAutoConfiguration.class
}, properties = {
    "cas.authn.redis.host=localhost",
    "cas.authn.redis.port=6379",
    "cas.authn.redis.password-encoder.type=DEFAULT",
    "cas.authn.redis.password-encoder.encoding-algorithm=SHA-512"
})
@EnableScheduling
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 6379)
class RedisAuthenticationHandlerTests {

    @Autowired
    @Qualifier("redisAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("authenticationRedisTemplate")
    private CasRedisTemplate authenticationRedisTemplate;

    @BeforeEach
    void initialize() {
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
    void verifyNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("123456", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyInvalid() {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casuser", "badpassword"), mock(Service.class)));
    }

    @Test
    void verifyExpired() {
        assertThrows(AccountExpiredException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casexpired", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyLocked() {
        assertThrows(AccountLockedException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caslocked", "caspassword"), mock(Service.class)));
    }

    @Test
    void verifyChangePsw() {
        assertThrows(AccountPasswordMustChangeException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caschangepsw", "caspassword"), mock(Service.class)));
    }
}
