package org.apereo.cas.authentication.principal;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyDelegatedClientAuthenticationCredentialResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.profile-selection.groovy.location=classpath:GroovyProfileSelection.groovy")
class GroovyDelegatedClientAuthenticationCredentialResolverTests {
    @Autowired
    @Qualifier("groovyDelegatedClientAuthenticationCredentialResolver")
    private DelegatedClientAuthenticationCredentialResolver groovyDelegatedClientAuthenticationCredentialResolver;

    @Test
    void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FacebookClient");
        assertTrue(groovyDelegatedClientAuthenticationCredentialResolver.supports(clientCredential));
        val results = groovyDelegatedClientAuthenticationCredentialResolver.resolve(context, clientCredential);
        assertEquals(1, results.size());
        val profile = results.get(0);
        assertEquals("casuser", profile.getLinkedId());
        assertEquals("resolved-casuser", profile.getId());
        assertTrue(profile.getAttributes().containsKey("memberOf"));
        assertTrue(profile.getAttributes().containsKey("uid"));
        DisposableBean.class.cast(groovyDelegatedClientAuthenticationCredentialResolver).destroy();
    }
}
