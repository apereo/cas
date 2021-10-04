package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRestAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasRestAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    RefreshAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreConfiguration.class
},
    properties = "cas.authn.rest.uri=http://localhost:8081/authn")
@Tag("RestfulApiAuthentication")
public class RestAuthenticationHandlerTests {
    @Autowired
    @Qualifier("restAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Test
    public void verifySuccess() throws Exception {
        val instant = Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.DAYS);
        val formatted = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(instant);

        val headers = new HashMap<String, String>();
        headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE, formatted);
        headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_WARNING, "warning1");

        try (val webServer = new MockWebServer(8081, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser"), headers, HttpStatus.OK)) {
            webServer.start();
            val res = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            assertEquals("casuser", res.getPrincipal().getId());
        }
    }

    @Test
    public void verifyNoPrincipal() {
        try (val webServer = new MockWebServer(8081, StringUtils.EMPTY)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyDisabledAccount() {
        try (val webServer = new MockWebServer(8081, HttpStatus.FORBIDDEN)) {
            webServer.start();
            assertThrows(AccountDisabledException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyUnauthorized() {
        try (val webServer = new MockWebServer(8081, HttpStatus.UNAUTHORIZED)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyOther() {
        try (val webServer = new MockWebServer(8081, HttpStatus.REQUEST_TIMEOUT)) {
            webServer.start();
            assertThrows(FailedLoginException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyLocked() {
        try (val webServer = new MockWebServer(8081, HttpStatus.LOCKED)) {
            webServer.start();
            assertThrows(AccountLockedException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyConditionReq() {
        try (val webServer = new MockWebServer(8081, HttpStatus.PRECONDITION_REQUIRED)) {
            webServer.start();
            assertThrows(AccountPasswordMustChangeException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyConditionFail() {
        try (val webServer = new MockWebServer(8081, HttpStatus.PRECONDITION_FAILED)) {
            webServer.start();
            assertThrows(AccountExpiredException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }

    @Test
    public void verifyNotFound() {
        try (val webServer = new MockWebServer(8081, HttpStatus.NOT_FOUND)) {
            webServer.start();
            assertThrows(AccountNotFoundException.class,
                () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        }
    }
}



