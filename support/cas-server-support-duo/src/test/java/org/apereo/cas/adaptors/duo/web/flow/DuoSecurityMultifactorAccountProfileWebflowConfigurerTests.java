package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.*;

/**
 * This is {@link DuoSecurityMultifactorAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowMfaConfig")
@Getter
@Import({
    BaseDuoSecurityTests.SharedTestConfiguration.class,
    DuoSecurityMultifactorAccountProfileWebflowConfigurerTests.AccountProfileTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
    "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
    "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
    "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
})
public class DuoSecurityMultifactorAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("accountProfileFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Autowired
    @Qualifier("duoMultifactorAuthenticationDeviceProviderAction")
    private MultifactorAuthenticationDeviceProviderAction duoMultifactorAuthenticationDeviceProviderAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val result = duoMultifactorAuthenticationDeviceProviderAction.execute(context);
        assertNull(result);
        assertNotNull(WebUtils.getMultifactorAuthenticationRegisteredDevices(context));
    }

    @TestConfiguration("AccountProfileTestConfiguration")
    public static class AccountProfileTestConfiguration {

        @Bean
        public FlowDefinitionRegistry accountProfileFlowRegistry() {
            val viewState = mock(ViewState.class);
            when(viewState.getRenderActionList()).thenReturn(new ActionList());

            val flowDefn = mock(Flow.class);
            when(flowDefn.containsState(anyString())).thenReturn(true);
            when(flowDefn.getState(anyString())).thenReturn(viewState);

            val registry = mock(FlowDefinitionRegistry.class);
            when(registry.getFlowDefinitionIds()).thenReturn(new String[]{CasWebflowConfigurer.FLOW_ID_ACCOUNT});
            when(registry.getFlowDefinition(anyString())).thenReturn(flowDefn);
            return registry;
        }
    }
}
