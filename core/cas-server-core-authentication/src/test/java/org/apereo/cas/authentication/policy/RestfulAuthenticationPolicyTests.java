package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashSet;

import static org.apereo.cas.util.junit.Assertions.assertThrowsWithRootCause;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
})
@DirtiesContext
@Tag("RestfulApi")
public class RestfulAuthenticationPolicyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyAllowedOperation() {
        try (val webServer = new MockWebServer(9200,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val policy = new RestfulAuthenticationPolicy("http://localhost:9200");
            assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"), new LinkedHashSet<>(), applicationContext));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyStatusCodeUnAuthz() {
        assertPolicyFails(9201, HttpStatus.UNAUTHORIZED, FailedLoginException.class);
        assertPolicyFails(9202, HttpStatus.LOCKED, AccountLockedException.class);
        assertPolicyFails(9203, HttpStatus.METHOD_NOT_ALLOWED, AccountDisabledException.class);
        assertPolicyFails(9204, HttpStatus.FORBIDDEN, AccountDisabledException.class);
        assertPolicyFails(9205, HttpStatus.NOT_FOUND, AccountNotFoundException.class);
        assertPolicyFails(9206, HttpStatus.PRECONDITION_FAILED, AccountExpiredException.class);
        assertPolicyFails(9207, HttpStatus.PRECONDITION_REQUIRED, AccountPasswordMustChangeException.class);
        assertPolicyFails(9208, HttpStatus.INTERNAL_SERVER_ERROR, FailedLoginException.class);
    }

    private void assertPolicyFails(final int port, final HttpStatus status,
                                   final Class<? extends Throwable> exceptionClass) {
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), status)) {
            webServer.start();
            val policy = new RestfulAuthenticationPolicy("http://localhost:" + port);
            assertThrowsWithRootCause(GeneralSecurityException.class, exceptionClass,
                () -> assertTrue(policy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser"),
                    new LinkedHashSet<>(), applicationContext)));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
