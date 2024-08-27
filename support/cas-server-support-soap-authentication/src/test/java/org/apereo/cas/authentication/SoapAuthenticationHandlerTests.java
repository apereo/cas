package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationResponse;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSoapAuthenticationAutoConfiguration;
import org.apereo.cas.config.SoapAuthenticationServerTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    SoapAuthenticationServerTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasSoapAuthenticationAutoConfiguration.class
},
    properties = {
        "server.port=8080",
        "cas.authn.soap.url=http://localhost:8080/ws/users"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("AuthenticationHandler")
@ExtendWith(CasTestExtension.class)
class SoapAuthenticationHandlerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("soapAuthenticationAuthenticationHandler")
    private AuthenticationHandler soapAuthenticationAuthenticationHandler;

    @Test
    void verifyAction() throws Throwable {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = soapAuthenticationAuthenticationHandler.authenticate(creds, mock(Service.class));
        assertNotNull(result);
        assertEquals("CAS", result.getPrincipal().getId());
        assertEquals(1, result.getPrincipal().getAttributes().size());
        assertTrue(result.getPrincipal().getAttributes().containsKey("givenName"));
    }

    @Test
    void verifyFailures() throws Throwable {
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
        assertThrows(clazz, () -> result.authenticate(creds, mock(Service.class)));
    }
}
