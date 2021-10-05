package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.support.inwebo.service.InweboService;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseInweboConfiguration.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.inwebo.client-certificate.certificate.location=classpath:clientcert.p12",
        "cas.authn.mfa.inwebo.client-certificate.passphrase=password",

        "cas.authn.mfa.inwebo.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.inwebo.bypass.authentication-method-name=nothing",
        "cas.authn.mfa.inwebo.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.inwebo.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.inwebo.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.inwebo.bypass.rest.url=http://localhost:8080/bypass"
    })
@Tag("MFAProvider")
public class InweboConfigurationTests {
     @Autowired
     @Qualifier("inweboService")
     private InweboService inweboService;

    @Autowired
    @Qualifier("inweboBypassEvaluator")
    private MultifactorAuthenticationProviderBypassEvaluator inweboBypassEvaluator;

    @Test
    public void verifyOperation() {
        assertNotNull(inweboBypassEvaluator);
        assertNotNull(inweboService);
    }
}
