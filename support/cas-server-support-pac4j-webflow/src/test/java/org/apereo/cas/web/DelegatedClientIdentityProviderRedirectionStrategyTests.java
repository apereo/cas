package org.apereo.cas.web;

import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-redirection-strategy.location=classpath:/GroovyClientRedirectStrategy.groovy")
@Tag("Delegation")
public class DelegatedClientIdentityProviderRedirectionStrategyTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderRedirectionStrategy")
    private DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy;

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val provider = DelegatedClientIdentityProviderConfiguration.builder()
            .name("SomeClient")
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
        val result = delegatedClientIdentityProviderRedirectionStrategy.getPrimaryDelegatedAuthenticationProvider(context, null, provider);
        assertTrue(result.isEmpty());
    }
}
