package org.apereo.cas.web.flow;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
 * This is {@link DelegatedClientIdentityProviderConfigurationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
public class DelegatedClientIdentityProviderConfigurationPostProcessorTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
    private DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    @Test
    public void verifyOperation() {
        assertNotNull(delegatedClientIdentityProviderConfigurationPostProcessor);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val context = new MockRequestContext();
                val request = new MockHttpServletRequest();
                val response = new MockHttpServletResponse();
                context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
                RequestContextHolder.setRequestContext(context);
                ExternalContextHolder.setExternalContext(context.getExternalContext());
                delegatedClientIdentityProviderConfigurationPostProcessor.process(context, Set.of());
                delegatedClientIdentityProviderConfigurationPostProcessor.destroy();
            }
        });

    }
}
