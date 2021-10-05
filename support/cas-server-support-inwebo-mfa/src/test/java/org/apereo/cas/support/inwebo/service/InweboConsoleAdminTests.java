package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.support.inwebo.config.BaseInweboConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ws.client.WebServiceTransportException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboConsoleAdminTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
@SpringBootTest(classes = BaseInweboConfiguration.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.inwebo.client-certificate.certificate.location=classpath:clientcert.p12",
        "cas.authn.mfa.inwebo.client-certificate.passphrase=password",
        "cas.authn.mfa.inwebo.service-id=7046"
    })
public class InweboConsoleAdminTests {
    @Autowired
    @Qualifier("inweboConsoleAdmin")
    private InweboConsoleAdmin inweboConsoleAdmin;

    @Test
    public void verifyOperation() {
        assertThrows(WebServiceTransportException.class, () -> inweboConsoleAdmin.loginSearch("casuser"));
    }
}
