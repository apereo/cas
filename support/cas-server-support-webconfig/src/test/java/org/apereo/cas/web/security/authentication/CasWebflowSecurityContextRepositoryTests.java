package org.apereo.cas.web.security.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.executor.CasFlowExecutor;
import org.apereo.cas.web.security.BaseWebSecurityTests;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebflowSecurityContextRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(classes = {
    CasWebflowSecurityContextRepositoryTests.WebflowTestConfiguration.class,
    BaseWebSecurityTests.SharedTestConfiguration.class
})
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
class CasWebflowSecurityContextRepositoryTests {
    private static final String USERNAME = UUID.randomUUID().toString();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("securityContextRepository")
    private SecurityContextRepository securityContextRepository;

    @Test
    void verifyNoContext() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        assertFalse(securityContextRepository.containsContext(requestContext.getHttpServletRequest()));
        val secContext = securityContextRepository.loadDeferredContext(requestContext.getHttpServletRequest());
        assertDoesNotThrow(() -> securityContextRepository.saveContext(secContext.get(),
            requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse()));
    }

    @Test
    void verifyWebflowAuthenticated() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setParameter(CasDefaultFlowUrlHandler.DEFAULT_FLOW_EXECUTION_KEY_PARAMETER, UUID.randomUUID().toString());
        assertTrue(securityContextRepository.containsContext(requestContext.getHttpServletRequest()));
        val secContext = securityContextRepository.loadDeferredContext(requestContext.getHttpServletRequest()).get();
        val principal = (User) secContext.getAuthentication().getPrincipal();
        assertEquals(USERNAME, principal.getUsername());
        assertEquals(USERNAME, secContext.getAuthentication().getName());
        assertTrue(secContext.getAuthentication().getAuthorities().size() > 1);
        assertTrue(secContext.getAuthentication().getAuthorities().stream()
            .anyMatch(auth -> "ROLE_USER".equalsIgnoreCase(auth.getAuthority())));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class WebflowTestConfiguration {
        @Bean
        public FlowExecutor loginFlowExecutor() {
            val conversationScope = new LocalAttributeMap<>();
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(USERNAME), conversationScope);
            val flowExecution = mock(FlowExecution.class);
            when(flowExecution.getConversationScope()).thenReturn(conversationScope);
            val executor = mock(CasFlowExecutor.class);
            val repository = mock(FlowExecutionRepository.class);
            when(repository.parseFlowExecutionKey(anyString())).thenReturn(mock(FlowExecutionKey.class));
            when(repository.getFlowExecution(any(FlowExecutionKey.class))).thenReturn(flowExecution);
            when(executor.getFlowExecutionRepository()).thenReturn(repository);
            when(executor.getFlowUrlHandler()).thenReturn(new CasDefaultFlowUrlHandler(List.of()));
            return executor;
        }
    }
}
