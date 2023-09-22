package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityPrepareWebLoginFormActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DuoSecurityPrepareWebLoginFormActionTests.MultifactorTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
class DuoSecurityPrepareWebLoginFormActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("dummyProvider")
    private MultifactorAuthenticationProvider dummyProvider;

    @Test
    void verifyOperation() throws Throwable {
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
        
        val flow = new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN);
        flow.setApplicationContext(applicationContext);
        val flowSession = new MockFlowSession(flow);
        flowSession.setState(new ViewState(flowSession.getDefinitionInternal(), "viewState", mock(ViewFactory.class)));
        val exec = new MockFlowExecutionContext(flowSession);
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(exec);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);

        WebUtils.putCredential(context, new DuoSecurityCredential(authentication.getPrincipal().getId(),
            UUID.randomUUID().toString(), DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER));
        WebUtils.putMultifactorAuthenticationProvider(context, dummyProvider);

        val action = new DuoSecurityPrepareWebLoginFormAction();
        val event = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
    static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider(final CasConfigurationProperties casProperties) {
            val duoService = mock(DuoSecurityAuthenticationService.class);
            when(duoService.getProperties()).thenReturn(
                new DuoSecurityMultifactorAuthenticationProperties()
                    .setDuoApiHost("theapi.duosecurity.com")
                    .setDuoIntegrationKey(UUID.randomUUID().toString())
                    .setDuoSecretKey(UUID.randomUUID().toString()));
            val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(provider.getDuoAuthenticationService()).thenReturn(duoService);
            when(provider.matches(anyString())).thenReturn(Boolean.TRUE);
            return provider;
        }
    }
}
