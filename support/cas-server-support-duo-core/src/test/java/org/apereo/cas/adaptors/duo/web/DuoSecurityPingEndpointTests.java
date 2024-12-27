package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityPingEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
class DuoSecurityPingEndpointTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() {
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

        val indicator = new DuoSecurityPingEndpoint(casProperties, this.applicationContext);
        val result = indicator.pingDuo(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        assertNotNull(result);
        assertTrue(result.containsKey(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER));
    }
}
