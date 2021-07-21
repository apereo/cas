package org.apereo.cas.config;

import org.apereo.cas.adaptors.authy.BaseAuthyAuthenticationTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthyAuthenticationMultifactorProviderBypassConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseAuthyAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.authy.api-key=example",
        "cas.authn.mfa.authy.api-url=http://localhost:8080/authy",
        "cas.authn.mfa.authy.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.authy.bypass.authentication-attribute-name=nothing",
        "cas.authn.mfa.authy.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.authy.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.authy.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.authy.bypass.rest.url=http://localhost:8080/bypass"
    })
@Tag("MFATrigger")
public class AuthyAuthenticationMultifactorProviderBypassConfigurationTests {
    @Autowired
    @Qualifier("authyBypassEvaluator")
    private MultifactorAuthenticationProviderBypassEvaluator authyBypassEvaluator;

    @Test
    public void verifyOperation() {
        assertNotNull(authyBypassEvaluator);
        assertEquals(8, authyBypassEvaluator.size());
    }
}
