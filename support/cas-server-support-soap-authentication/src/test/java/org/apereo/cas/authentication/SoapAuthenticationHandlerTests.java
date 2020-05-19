package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.SoapAuthenticationConfiguration;
import org.apereo.cas.config.SoapAuthenticationServerTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.SneakyThrows;
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

import static org.junit.jupiter.api.Assertions.*;

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
    SoapAuthenticationConfiguration.class
},
    properties = {
        "server.port=8080",
        "cas.authn.soap.url=http://localhost:8080/ws/users"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class SoapAuthenticationHandlerTests {
    @Autowired
    @Qualifier("soapAuthenticationAuthenticationHandler")
    private AuthenticationHandler soapAuthenticationAuthenticationHandler;

    @Test
    @SneakyThrows
    public void verifyAction() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = soapAuthenticationAuthenticationHandler.authenticate(creds);
        assertNotNull(result);
        assertEquals("CAS", result.getPrincipal().getId());
        assertEquals(1, result.getPrincipal().getAttributes().size());
        assertTrue(result.getPrincipal().getAttributes().containsKey("givenName"));
    }
}
