package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.yubikey.bypass.authentication-attribute-name=nothing",
        "cas.authn.mfa.yubikey.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.yubikey.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.yubikey.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.yubikey.bypass.rest.url=http://localhost:8080/bypass"
    })
@Tag("MFATrigger")
public class YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests {
    @Autowired
    @Qualifier("yubikeyBypassEvaluator")
    private MultifactorAuthenticationProviderBypassEvaluator yubikeyBypassEvaluator;

    @Test
    public void verifyOperation() {
        assertNotNull(yubikeyBypassEvaluator);
        assertEquals(8, yubikeyBypassEvaluator.size());
    }


}
