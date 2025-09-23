package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseMultifactorAuthenticationTriggerTests {
    protected GeoLocationService geoLocationService;

    protected Authentication authentication;

    protected RegisteredService registeredService;

    protected MockHttpServletRequest httpRequest;

    protected MockHttpServletResponse httpResponse;

    protected TestMultifactorAuthenticationProvider multifactorAuthenticationProvider;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @BeforeEach
    void setup(final TestInfo info) {
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
        this.httpRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        var clientInfo = ClientInfo.from(this.httpRequest);
        ClientInfoHolder.setClientInfo(clientInfo);

        this.httpResponse = new MockHttpServletResponse();
    }
}
