package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = BaseRestAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.rest[0].uri=http://localhost:${random.int[3000,9000]}/authn")
@Tag("RestfulApiAuthentication")
@ExtendWith(CasTestExtension.class)
class RestAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("restAuthenticationHandler")
    private BeanContainer<AuthenticationHandler> authenticationHandler;

    private AuthenticationHandler getFirstHandler() {
        return authenticationHandler.first();
    }

    @Test
    void verifyOperations() throws Throwable {
        val port = URI.create(casProperties.getAuthn().getRest().getFirst().getUri()).getPort();
        val instant = Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.DAYS);
        val formatted = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(instant);


        try (val webServer = new MockWebServer(port)) {
            webServer.start();
            webServer.responseBodyJson(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser"));

            val headers = new HashMap<String, String>();
            headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE, formatted);
            headers.put(RestAuthenticationHandler.HEADER_NAME_CAS_WARNING, "warning1");

            webServer.headers(headers);
            webServer.responseStatus(HttpStatus.OK);

            val res = getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class));
            assertEquals("casuser", res.getPrincipal().getId());

            webServer.responseBody("{}");
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.FORBIDDEN);
            assertThrows(AccountDisabledException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));


            webServer.responseStatus(HttpStatus.UNAUTHORIZED);
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.REQUEST_TIMEOUT);
            assertThrows(FailedLoginException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.LOCKED);
            assertThrows(AccountLockedException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.PRECONDITION_REQUIRED);
            assertThrows(AccountPasswordMustChangeException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.PRECONDITION_FAILED);
            assertThrows(AccountExpiredException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));

            webServer.responseStatus(HttpStatus.NOT_FOUND);
            assertThrows(AccountNotFoundException.class,
                () -> getFirstHandler().authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
        }
    }
}



