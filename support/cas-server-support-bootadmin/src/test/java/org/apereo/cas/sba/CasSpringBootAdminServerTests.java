package org.apereo.cas.sba;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasSpringBootAdminAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientCustomizer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    WebClientAutoConfiguration.class,
    AdminServerAutoConfiguration.class,

    SpringBootAdminClientAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasSpringBootAdminAutoConfiguration.class
}, properties = {
    "cas.host.name=CASInstance",
    "spring.boot.admin.client.url=https://localhost:8443/cas",
    "spring.boot.admin.client.username=casuser",
    "spring.boot.admin.client.password=Mellon"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class CasSpringBootAdminServerTests {
    @Autowired
    @Qualifier("springBootAdminEndpointConfigurer")
    private CasWebSecurityConfigurer<HttpSecurity> springBootAdminEndpointConfigurer;

    @Autowired
    @Qualifier("springBootAdminWebClientCustomizer")
    private InstanceWebClientCustomizer springBootAdminWebClientCustomizer;

    @Autowired
    @Qualifier("registrationClient")
    private RegistrationClient registrationClient;

    @Autowired
    @Qualifier("instanceIdGenerator")
    private InstanceIdGenerator instanceIdGenerator;

    @Test
    void verifyOperation() throws Throwable {
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

    @Test
    void verifyInstanceIdGeneration() {
        val registration1 = Registration.create("Cas1", "https://localhost:8443/cas/actuator/health")
            .metadata("name", "CASInstance").build();
        val registration2 = Registration.create("Cas2", "https://localhost:8443/cas/actuator/health").build();
        val id1 = instanceIdGenerator.generateId(registration1);
        val id2 = instanceIdGenerator.generateId(registration2);
        assertEquals(id1, id2);
    }
}
