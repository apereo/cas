package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.session.TicketRegistrySessionRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.session.autoconfigure.SessionAutoConfiguration;
import org.springframework.boot.session.autoconfigure.SessionsEndpointAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TicketRegistrySessionRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@SpringBootTest(classes = {

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
    CasThemesAutoConfiguration.class,
    CasTicketRegistrySessionAutoConfiguration.class,

    SessionAutoConfiguration.class,
    SessionsEndpointAutoConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.sessions.access=UNRESTRICTED"
    }
)
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
    private FindByIndexNameSessionRepository<MapSession> sessionRepository;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory ticketFactory;

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
        mockMvc.perform(get("/actuator/sessions")
                .queryParam("username", "casuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessions").exists());

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
        mockMvc.perform(get("/actuator/sessions/" + mapSession.getId()))
            .andExpect(status().isOk());

    }

    @Test
    void verifySessionFollowsStoredTicket() throws Throwable {
        val registry = mock(TicketRegistry.class);
        when(registry.getTicket(anyString(), eq(TransientSessionTicket.class))).thenThrow(new InvalidTicketException("TST-missing"));
        val addedTicket = mock(Ticket.class);
        when(addedTicket.getId()).thenReturn("TST-stored");
        when(registry.addTicket(any(Ticket.class))).thenReturn(addedTicket);
        val repository = new TicketRegistrySessionRepository(new DirectObjectProvider<>(registry), new DirectObjectProvider<>(ticketFactory));

        val session = new MapSession();
        session.setAttribute("roles", new ArrayList<>(List.of("admin")));
        repository.save(session);
        assertEquals("TST-stored", session.getId());

        val captor = ArgumentCaptor.forClass(Ticket.class);
        verify(registry).addTicket(captor.capture());
        val storedTicket = (TransientSessionTicket) captor.getValue();
        assertTrue(storedTicket.getProperties().values().stream().filter(Objects::nonNull).allMatch(String.class::isInstance));

        doReturn(storedTicket).when(registry).getTicket("TST-stored", TransientSessionTicket.class);
        val foundSession = Objects.requireNonNull(repository.findById("TST-stored"));
        assertEquals(List.of("admin"), foundSession.getAttribute("roles"));
        assertEquals(session.getCreationTime(), foundSession.getCreationTime());
        assertEquals(session.getLastAccessedTime(), foundSession.getLastAccessedTime());

        val updatedTicket = mock(Ticket.class);
        when(updatedTicket.getId()).thenReturn("TST-updated");
        when(registry.updateTicket(any(Ticket.class))).thenReturn(updatedTicket);
        session.setAttribute("roles", new ArrayList<>(List.of("auditor")));
        repository.save(session);
        assertEquals("TST-updated", session.getId());
        verify(registry).updateTicket(storedTicket);
        assertEquals(List.of("auditor"), Objects.requireNonNull(repository.findById("TST-stored")).getAttribute("roles"));
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
                session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, "casuser");
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
