package org.apereo.cas.adaptors.duo.web.flow;

import module java.base;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserDevice;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAdminApiService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationDeviceManager;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityMultifactorAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@Getter
@Import({
    DuoSecurityMultifactorAccountProfileWebflowConfigurerTests.DuoSecurityTestConfiguration.class,
    BaseDuoSecurityTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "CasFeatureModule.AccountManagement.enabled=true",
    "cas.authn.mfa.duo[0].duo-secret-key=cGKL1OndjtknbmVOWaFmisaghiNFEKXHxgXCJEBr",
    "cas.authn.mfa.duo[0].duo-integration-key=DIZXVRQD3OMZ6XXMNFQ9",
    "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
})
class DuoSecurityMultifactorAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("duoMultifactorAuthenticationDeviceProviderAction")
    private MultifactorAuthenticationDeviceProviderAction duoMultifactorAuthenticationDeviceProviderAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val result = duoMultifactorAuthenticationDeviceProviderAction.execute(context);
        assertNull(result);
        val devices = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationRegisteredDevices(context);
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @TestConfiguration(value = "DuoSecurityTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityTestConfiguration {
        @Bean
        public DuoSecurityMultifactorAuthenticationProvider dummyDuoSecurityProvider() throws Exception {
            val acct = new DuoSecurityUserAccount(UUID.randomUUID().toString())
                .setProviderId(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            acct.setDevices(List.of(DuoSecurityUserDevice.builder()
                .id(UUID.randomUUID().toString())
                .model("Samsung S20")
                .name("My Device")
                .number("1234567890")
                .platform("Android")
                .type("Android Phone - Google")
                .build()));
            val adminApi = mock(DuoSecurityAdminApiService.class);
            when(adminApi.getDuoSecurityUserAccount(anyString())).thenReturn(Optional.of(acct));
            val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            val duoService = mock(DuoSecurityAuthenticationService.class);
            when(duoService.getAdminApiService()).thenReturn(Optional.of(adminApi));
            when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(provider.getDuoAuthenticationService()).thenReturn(duoService);
            when(provider.getDeviceManager()).thenReturn(new DuoSecurityMultifactorAuthenticationDeviceManager(provider));
            return provider;
        }

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
