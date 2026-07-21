package org.apereo.cas.adaptors.duo.web;

import module java.base;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DuoSecurityPingEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    DuoSecurityPingEndpointTests.DuoSecurityPingTestConfiguration.class,
    CasCoreWebAutoConfiguration.class
}, properties = {
    "management.endpoint.duoPing.access=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
class DuoSecurityPingEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @TestConfiguration(value = "DuoSecurityPingTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityPingTestConfiguration {
        @Bean
        public DuoSecurityPingEndpoint duoSecurityPingEndpoint(final CasConfigurationProperties casProperties,
                                                              final ApplicationContext applicationContext) {
            return new DuoSecurityPingEndpoint(casProperties, applicationContext);
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.ping()).thenReturn(true);

        val props = new DuoSecurityMultifactorAuthenticationProperties()
            .setDuoApiHost("https://api.duosecurity.com");
        when(duoService.getProperties()).thenReturn(props);

        val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(bean.getDuoAuthenticationService()).thenReturn(duoService);
        when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bean, "duoProvider");

        mockMvc.perform(get("/actuator/duoPing")
                .param("providerId", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['%s']".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).exists());
    }
}
