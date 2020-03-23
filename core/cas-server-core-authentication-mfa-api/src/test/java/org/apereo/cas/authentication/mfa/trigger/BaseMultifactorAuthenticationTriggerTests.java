package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@DirtiesContext
@Tag("MFA")
public abstract class BaseMultifactorAuthenticationTriggerTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    protected GeoLocationService geoLocationService;

    protected Authentication authentication;

    protected RegisteredService registeredService;

    protected MockHttpServletRequest httpRequest;

    protected TestMultifactorAuthenticationProvider multifactorAuthenticationProvider;

    @BeforeEach
    public void setup(final TestInfo info) {
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        if (!info.getTags().contains("DisableProviderRegistration")) {
            this.multifactorAuthenticationProvider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        }

        this.authentication = mock(Authentication.class);
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(principal.getAttributes()).thenReturn(Map.of("email", List.of("casuser@example.org")));
        when(authentication.getAttributes()).thenReturn(Map.of("category", List.of("user-object"), "mfa-mode", List.of("mfa-other")));
        when(authentication.getPrincipal()).thenReturn(principal);

        this.registeredService = mock(RegisteredService.class);
        when(registeredService.getName()).thenReturn("Service");
        when(registeredService.getServiceId()).thenReturn("http://app.org");
        when(registeredService.getId()).thenReturn(1000L);

        this.geoLocationService = mock(GeoLocationService.class);

        this.httpRequest = new MockHttpServletRequest();
        this.httpRequest.setRemoteAddr("185.86.151.11");
        this.httpRequest.setLocalAddr("185.88.151.12");
        this.httpRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        ClientInfoHolder.setClientInfo(new ClientInfo(this.httpRequest));
    }
}
