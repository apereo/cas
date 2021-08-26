package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.radius.web.flow.BaseRadiusMultifactorAuthenticationTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RadiusTokenAuthenticationMultifactorProviderBypassConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.client.shared-secret=NoSecret",
        "cas.authn.mfa.radius.client.inet-address=localhost,localguest",

        "cas.authn.mfa.radius.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.radius.bypass.authentication-method-name=radius",
        "cas.authn.mfa.radius.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.radius.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.radius.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.radius.bypass.rest.url=http://localhost:8080/bypass"
    })
@Tag("Radius")
public class RadiusTokenAuthenticationMultifactorProviderBypassConfigurationTests {
    @Autowired
    @Qualifier("radiusBypassEvaluator")
    private MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator;

    @Test
    public void verifyOperation() {
        assertNotNull(radiusBypassEvaluator);
        assertEquals(8, radiusBypassEvaluator.size());
    }
}
