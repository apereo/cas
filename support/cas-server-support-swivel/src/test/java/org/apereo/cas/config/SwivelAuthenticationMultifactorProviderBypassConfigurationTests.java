package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.BaseSwivelAuthenticationTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SwivelAuthenticationMultifactorProviderBypassConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
@SpringBootTest(classes = BaseSwivelAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.swivel.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.swivel.bypass.authentication-attribute-name=nothing",
        "cas.authn.mfa.swivel.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.swivel.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.swivel.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.swivel.bypass.rest.url=http://localhost:8080/bypass"
    })
public class SwivelAuthenticationMultifactorProviderBypassConfigurationTests {
    @Autowired
    @Qualifier("swivelBypassEvaluator")
    private MultifactorAuthenticationProviderBypassEvaluator swivelBypassEvaluator;

    @Test
    public void verifyOperation() {
        assertNotNull(swivelBypassEvaluator);
        assertEquals(8, swivelBypassEvaluator.size());
    }

}
