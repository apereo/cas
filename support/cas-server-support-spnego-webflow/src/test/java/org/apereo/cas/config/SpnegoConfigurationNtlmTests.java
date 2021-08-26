package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.web.flow.AbstractSpnegoTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpnegoConfigurationNtlmTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Spnego")
@TestPropertySource(properties = {
    "cas.authn.spnego.properties[0].jcifs-username=casuser",
    "cas.authn.spnego.properties[0].jcifs-netbios-wins=netbios",
    "cas.authn.ntlm.enabled=true"
})
public class SpnegoConfigurationNtlmTests extends AbstractSpnegoTests {
    @Autowired
    @Qualifier("ntlmAuthenticationHandler")
    private AuthenticationHandler ntlmAuthenticationHandler;

    @Test
    public void verifyOperation() {
        assertNotNull(ntlmAuthenticationHandler);
    }

}
