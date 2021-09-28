package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationResponse;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.SoapAuthenticationConfiguration;
import org.apereo.cas.config.SoapAuthenticationServerTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SoapAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    SoapAuthenticationServerTestConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    SoapAuthenticationConfiguration.class
},
    properties = {
        "server.port=8080",
        "cas.authn.soap.url=http://localhost:8080/ws/users"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("AuthenticationHandler")
public class SoapAuthenticationHandlerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("soapAuthenticationAuthenticationHandler")
    private AuthenticationHandler soapAuthenticationAuthenticationHandler;

    @Test
    public void verifyAction() throws Exception {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = soapAuthenticationAuthenticationHandler.authenticate(creds);
        assertNotNull(result);
        assertEquals("CAS", result.getPrincipal().getId());
        assertEquals(1, result.getPrincipal().getAttributes().size());
        assertTrue(result.getPrincipal().getAttributes().containsKey("givenName"));
    }

    @Test
    public void verifyFailures() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        runTest(creds, HttpStatus.FORBIDDEN, AccountDisabledException.class);
        runTest(creds, HttpStatus.UNAUTHORIZED, FailedLoginException.class);
        runTest(creds, HttpStatus.NOT_FOUND, AccountNotFoundException.class);
        runTest(creds, HttpStatus.LOCKED, AccountLockedException.class);
        runTest(creds, HttpStatus.PRECONDITION_FAILED, AccountExpiredException.class);
        runTest(creds, HttpStatus.PRECONDITION_REQUIRED, AccountPasswordMustChangeException.class);
        runTest(creds, HttpStatus.NOT_ACCEPTABLE, FailedLoginException.class);
    }

    private void runTest(final UsernamePasswordCredential creds, final HttpStatus status, final Class clazz) {
        var response = new GetSoapAuthenticationResponse();
        val client = mock(SoapAuthenticationClient.class);
        response.setStatus(status.value());
        when(client.sendRequest(any())).thenReturn(response);
        val result = new SoapAuthenticationHandler("Handler", servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(), 0, client);
        assertThrows(clazz, () -> result.authenticate(creds));
    }
}
