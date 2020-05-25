package org.apereo.cas.redis;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.RedisAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RedisAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.redis.host=localhost",
    "cas.authn.redis.port=6379",
    "cas.authn.redis.password-encoder.type=DEFAULT",
    "cas.authn.redis.password-encoder.encoding-algorithm=SHA-512"
})
@EnableScheduling
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
public class RedisAuthenticationHandlerTests {

    @Autowired
    @Qualifier("redisAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("authenticationRedisTemplate")
    private RedisTemplate authenticationRedisTemplate;

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
    public void verifySuccessful() throws Exception {
        val result = authenticationHandler.authenticate(new UsernamePasswordCredential("casuser", "caspassword"));
        assertNotNull(result);
        val principal = result.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getAttributes());
        assertTrue(principal.getAttributes().containsKey("name"));
        assertTrue(principal.getAttributes().containsKey("group"));
    }

    @Test
    public void verifyNotFound() {
        assertThrows(AccountNotFoundException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("123456", "caspassword")));
    }

    @Test
    public void verifyInvalid() {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casuser", "badpassword")));
    }

    @Test
    public void verifyExpired() {
        assertThrows(AccountExpiredException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("casexpired", "caspassword")));
    }

    @Test
    public void verifyLocked() {
        assertThrows(AccountLockedException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caslocked", "caspassword")));
    }

    @Test
    public void verifyChangePsw() {
        assertThrows(AccountPasswordMustChangeException.class,
            () -> authenticationHandler.authenticate(new UsernamePasswordCredential("caschangepsw", "caspassword")));
    }
}
