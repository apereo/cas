package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyDelegatedClientAuthenticationCredentialResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.profile-selection.groovy.location=classpath:GroovyProfileSelection.groovy")
class GroovyDelegatedClientAuthenticationCredentialResolverTests {
    @Autowired
    @Qualifier("groovyDelegatedClientAuthenticationCredentialResolver")
    private DelegatedClientAuthenticationCredentialResolver groovyDelegatedClientAuthenticationCredentialResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FakeClient");
        assertTrue(groovyDelegatedClientAuthenticationCredentialResolver.supports(clientCredential));
        val results = groovyDelegatedClientAuthenticationCredentialResolver.resolve(context, clientCredential);
        assertEquals(1, results.size());
        val profile = results.getFirst();
        assertEquals("casuser", profile.getLinkedId());
        assertEquals("resolved-casuser", profile.getId());
        assertTrue(profile.getAttributes().containsKey("memberOf"));
        assertTrue(profile.getAttributes().containsKey("uid"));
        ((DisposableBean) groovyDelegatedClientAuthenticationCredentialResolver).destroy();
    }
}
