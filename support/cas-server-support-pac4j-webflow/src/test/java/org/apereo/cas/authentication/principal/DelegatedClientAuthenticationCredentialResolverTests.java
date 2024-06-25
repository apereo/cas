package org.apereo.cas.authentication.principal;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationCredentialResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientAuthenticationCredentialResolverTests {
    @Autowired
    @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
    private DelegatedClientAuthenticationConfigurationContext configurationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(configurationContext.getApplicationContext());

        val resolver = new TestBaseDelegatedClientAuthenticationCredentialResolver(configurationContext);
        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FakeClient");
        assertTrue(resolver.supports(clientCredential));
        val results = resolver.resolve(context, clientCredential);
        assertEquals(1, results.size());
        val profile = results.getFirst();
        assertEquals("casuser-linked", profile.getLinkedId());
        assertEquals("casuser", profile.getId());
        assertTrue(profile.getAttributes().containsKey("memberOf"));
        assertTrue(profile.getAttributes().containsKey("uid"));
    }
}
