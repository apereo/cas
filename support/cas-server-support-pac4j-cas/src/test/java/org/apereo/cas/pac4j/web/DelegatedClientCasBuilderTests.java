package org.apereo.cas.pac4j.web;

import org.apereo.cas.config.CasDelegatedAuthenticationCasAutoConfiguration;
import org.apereo.cas.support.pac4j.clients.BaseDelegatedClientFactoryTests;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.cas.client.CasClient;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientCasBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@ImportAutoConfiguration(CasDelegatedAuthenticationCasAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.pac4j.cas[0].login-url=https://login.example.org/login",
    "cas.authn.pac4j.cas[0].protocol=SAML",
    "cas.authn.pac4j.cas[0].principal-id-attribute=uid",
    "cas.authn.pac4j.cas[0].css-class=cssClass",
    "cas.authn.pac4j.cas[0].display-name=My CAS",
    "cas.authn.pac4j.core.lazy-init=true",
    "cas.custom.properties.delegation-test.enabled=false"
})
class DelegatedClientCasBuilderTests extends BaseDelegatedClientFactoryTests {
    @Test
    void verifyFactoryForCasClientsHavingLoginInDomain() {
        val clients = delegatedIdentityProviderFactory.build();
        assertEquals(1, clients.size());
        val client = (CasClient) clients.getFirst();
        assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
    }
}
