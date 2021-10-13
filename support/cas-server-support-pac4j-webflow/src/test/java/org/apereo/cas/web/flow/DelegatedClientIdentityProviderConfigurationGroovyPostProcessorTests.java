package org.apereo.cas.web.flow;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-provider-post-processor.location=classpath:GroovyClientProviderProcessor.groovy")
@Tag("Delegation")
public class DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
    private DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    @Autowired
    @Qualifier("builtClients")
    private Clients builtClients;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val client = builtClients.findClient("CasClient").get();
        val provider = DelegatedClientIdentityProviderConfiguration.builder().name(client.getName()).build();
        val clientConfig = Set.of(provider);
        delegatedClientIdentityProviderConfigurationPostProcessor.process(context, clientConfig);
        assertEquals("TestTitle", clientConfig.iterator().next().getTitle());
        delegatedClientIdentityProviderConfigurationPostProcessor.destroy();
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        assertTrue(provider.isAutoRedirect());
    }
}
