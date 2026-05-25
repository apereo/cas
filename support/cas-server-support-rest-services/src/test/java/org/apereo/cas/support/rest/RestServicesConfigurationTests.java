package org.apereo.cas.support.rest;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRestServicesAutoConfiguration;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RestServicesConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasRestServicesAutoConfiguration.class
}, properties = {
    "cas.rest.services.attribute-name=attr-name",
    "cas.rest.services.attribute-value=attr-v"
})
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
class RestServicesConfigurationTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(mockMvc);
        mockMvc.perform(post("/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serializeRegisteredService()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyOperationWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic Og==")
                .content(serializeRegisteredService()))
            .andExpect(status().isUnauthorized());
    }

    private static String serializeRegisteredService() {
        try (var applicationContext = new StaticApplicationContext()) {
            applicationContext.refresh();
            var service = new CasRegisteredService();
            service.setId(1000);
            service.setName("TestService");
            service.setServiceId("https://example.org/.+");
            return new RegisteredServiceJsonSerializer(applicationContext).toString(service);
        }
    }
}
