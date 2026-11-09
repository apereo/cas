package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasInterruptAutoConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link InterruptInquiryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@TestPropertySource(properties = "management.endpoint.interrupts.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration(CasInterruptAutoConfiguration.class)
@Import(InterruptInquiryEndpointTests.InterruptInquiryEndpointTestConfiguration.class)
class InterruptInquiryEndpointTests extends AbstractCasEndpointTests {
    private static final String SERVICE = "https://interrupts.example.org/app";

    @Test
    void verifyInterruptForUserAndService() throws Throwable {
        mockMvc.perform(get("/actuator/interrupts/{username}", "interrupted-casuser")
                .param("service", SERVICE)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.principal").value("interrupted-casuser"))
            .andExpect(jsonPath("$.service").value(SERVICE))
            .andExpect(jsonPath("$.inquirers.length()").value(3))
            .andExpect(jsonPath("$.inquirers[0].name").value("FailingInquirer"))
            .andExpect(jsonPath("$.inquirers[0].error").value("Interrupt endpoint is unavailable"))
            .andExpect(jsonPath("$.inquirers[0].response").doesNotExist())
            .andExpect(jsonPath("$.inquirers[1].name").value("TermsInquirer"))
            .andExpect(jsonPath("$.inquirers[1].response.message").value("Accept the terms of " + SERVICE + " as interrupted-casuser"))
            .andExpect(jsonPath("$.inquirers[2].name").value("WelcomeInquirer"))
            .andExpect(jsonPath("$.inquirers[2].response.message").value("Welcome"))
            .andExpect(jsonPath("$.interrupt.message").value("Accept the terms of " + SERVICE + " as interrupted-casuser"))
            .andExpect(jsonPath("$.interrupt.block").value(true))
            .andExpect(jsonPath("$.interrupt.links.Terms").value("https://terms.example.org"));
    }

    @Test
    void verifyInterruptWithoutService() throws Throwable {
        mockMvc.perform(get("/actuator/interrupts/{username}", "casuser")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.principal").value("casuser"))
            .andExpect(jsonPath("$.service").doesNotExist())
            .andExpect(jsonPath("$.registeredService").doesNotExist())
            .andExpect(jsonPath("$.inquirers.length()").value(3))
            .andExpect(jsonPath("$.inquirers[1].response.message").doesNotExist())
            .andExpect(jsonPath("$.interrupt.message").value("Welcome"));
    }

    @Test
    void verifyServiceIsRefused() throws Throwable {
        mockMvc.perform(get("/actuator/interrupts/{username}", "casuser")
                .param("service", "https://casuser@interrupts.example.org/app")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @TestConfiguration(value = "InterruptInquiryEndpointTestConfiguration", proxyBeanMethods = false)
    static class InterruptInquiryEndpointTestConfiguration {
        @Bean
        public InterruptInquiryExecutionPlanConfigurer endpointTestInterruptInquiryExecutionPlanConfigurer() {
            return plan -> {
                plan.registerInterruptInquirer(new NamedInterruptInquirer("FailingInquirer",
                    (authentication, registeredService, service, credential) -> {
                        throw new IllegalStateException("Interrupt endpoint is unavailable");
                    }));
                plan.registerInterruptInquirer(new NamedInterruptInquirer("TermsInquirer",
                    (authentication, registeredService, service, credential) -> {
                        if (service == null || !authentication.getPrincipal().getId().startsWith("interrupted-")) {
                            return InterruptResponse.none();
                        }
                        return new InterruptResponse("Accept the terms of " + service.getId() + " as " + credential.getId())
                            .setLinks(Map.of("Terms", "https://terms.example.org"))
                            .setBlock(true);
                    }));
                plan.registerInterruptInquirer(new NamedInterruptInquirer("WelcomeInquirer",
                    (authentication, registeredService, service, credential) -> new InterruptResponse("Welcome")));
            };
        }
    }

    private record NamedInterruptInquirer(String name, InterruptInquirer inquiry) implements InterruptInquirer {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService,
                                         final Service service, final Credential credential)
                throws Throwable {
            return inquiry.inquire(authentication, registeredService, service, credential);
        }
    }
}
