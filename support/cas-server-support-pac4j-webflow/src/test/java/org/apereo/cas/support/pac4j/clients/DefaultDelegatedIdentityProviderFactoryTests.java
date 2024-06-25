package org.apereo.cas.support.pac4j.clients;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedIdentityProviderFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Delegation")
class DefaultDelegatedIdentityProviderFactoryTests {

    @TestPropertySource(properties = "cas.custom.properties.delegation-test.enabled=false")
    abstract static class BaseTests extends BaseDelegatedClientFactoryTests {
    }
    
    
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.cas[0].login-url=https://login.example.org/login",
        "cas.authn.pac4j.cas[0].protocol=SAML",
        "cas.authn.pac4j.cas[0].principal-id-attribute=uid",
        "cas.authn.pac4j.cas[0].css-class=cssClass",
        "cas.authn.pac4j.cas[0].display-name=My CAS",
        "cas.authn.pac4j.core.lazy-init=true"
    })
    class CasClients extends BaseTests {
        @Test
        void verifyFactoryForCasClientsHavingLoginInDomain() throws Throwable {
            val clients = delegatedIdentityProviderFactory.build();
            assertEquals(1, clients.size());
            val client = (CasClient) clients.iterator().next();
            assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
        }
    }

}
