package org.apereo.cas.authentication.principal;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.credentials.TokenCredentials;
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
 * This is {@link DelegatedClientAuthenticationCredentialResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public class DelegatedClientAuthenticationCredentialResolverTests {
    @Autowired
    @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
    private DelegatedClientAuthenticationConfigurationContext configurationContext;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val resolver = new TestBaseDelegatedClientAuthenticationCredentialResolver(configurationContext);
        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FacebookClient");
        assertTrue(resolver.supports(clientCredential));
        val results = resolver.resolve(context, clientCredential);
        assertEquals(1, results.size());
        val profile = results.get(0);
        assertEquals("casuser-linked", profile.getLinkedId());
        assertEquals("casuser", profile.getId());
        assertTrue(profile.getAttributes().containsKey("memberOf"));
        assertTrue(profile.getAttributes().containsKey("uid"));
    }
}
