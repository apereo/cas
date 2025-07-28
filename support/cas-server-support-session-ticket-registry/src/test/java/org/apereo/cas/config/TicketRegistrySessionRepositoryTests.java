package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TicketRegistrySessionRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    TicketRegistrySessionRepositoryTests.TicketRegistrySessionRepositoryTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasWebAppAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreValidationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasTicketRegistrySessionAutoConfiguration.class
})
@SpringBootTestAutoConfigurations
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
@Tag("Web")
class TicketRegistrySessionRepositoryTests {

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("sessionRepository")
    private SessionRepository sessionRepository;

    @Test
    void verifySaveOperation() throws Exception {
        mockMvc.perform(get("/session/set"))
            .andExpect(status().isOk())
            .andExpect(content().string("set"));
        mockMvc.perform(get("/session/invalidate"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/session/get"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyDelete() throws Exception {
        val result = mockMvc.perform(get("/session/set")).andReturn();
        val session = mockMvc.perform(get("/session/invalidate")
                .cookie(result.getResponse().getCookie("SESSION")))
            .andExpect(status().isOk())
            .andExpect(content().string("gone"))
            .andReturn()
            .getRequest()
            .getSession();

        val mapSession = new MapSession(session.getId());
        mapSession.setId(UUID.randomUUID().toString());
        sessionRepository.save(mapSession);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TicketRegistrySessionRepositoryTestConfiguration {
        @Bean
        public CasWebSecurityConfigurer<Void> casEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of("/session");
                }
            };
        }

        @RestController
        static class SessionTestController {

            @GetMapping("/session/set")
            public String setSession(final HttpSession session) {
                session.setAttribute("foo", "bar");
                return "set";
            }

            @GetMapping("/session/get")
            public String getSession(final HttpSession session) {
                return session.getId();
            }

            @GetMapping("/session/invalidate")
            public String invalidate(final HttpSession session) {
                session.invalidate();
                return "gone";
            }
        }
    }
}
