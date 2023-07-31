package org.apereo.cas.sba;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasSpringBootAdminServerConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerInstanceWebClientConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientCustomizer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasSpringBootAdminServerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebApp")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebClientAutoConfiguration.class,
    DispatcherServletAutoConfiguration.class,

    AdminServerMarkerConfiguration.class,
    AdminServerAutoConfiguration.class,
    AdminServerWebConfiguration.class,
    AdminServerInstanceWebClientConfiguration.class,

    SpringBootAdminClientAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasSpringBootAdminServerConfiguration.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.boot.admin.client.url=https://localhost:8443/cas",
    "spring.boot.admin.client.username=casuser",
    "spring.boot.admin.client.password=Mellon"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSpringBootAdminServerTests {
    @Autowired
    @Qualifier("springBootAdminEndpointConfigurer")
    private ProtocolEndpointWebSecurityConfigurer<HttpSecurity> springBootAdminEndpointConfigurer;

    @Autowired
    @Qualifier("springBootAdminWebClientCustomizer")
    private InstanceWebClientCustomizer springBootAdminWebClientCustomizer;

    @Autowired
    @Qualifier("registrationClient")
    private RegistrationClient registrationClient;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(springBootAdminEndpointConfigurer);
        assertNotNull(springBootAdminWebClientCustomizer);
        assertNotNull(registrationClient);

        val http = mock(HttpSecurity.class);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.logout(any())).thenReturn(http);
        val cfg = springBootAdminEndpointConfigurer.finish(http);
        assertNotNull(cfg);
    }
}
