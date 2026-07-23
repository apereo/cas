package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DashboardControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */

@SpringBootTest(classes = BasePalantirTests.SharedTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class DashboardControllerTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mvc;
    
    @Test
    void verifyOperation() throws Throwable {
        val authentication = new UsernamePasswordAuthenticationToken(
            "casuser",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        val model = mvc.perform(get("/palantir/dashboard")
                .with(authentication(authentication)))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView()
            .getModel();
        assertNotNull(model.get("casServerPrefix"));
        assertTrue(model.containsKey("authentication"));
        assertTrue(model.containsKey("httpRequestMethod"));
        assertTrue(model.containsKey("httpRequestSecure"));
        assertTrue(model.containsKey("actuatorEndpoints"));
        assertTrue(model.containsKey("serviceDefinitions"));

        mvc.perform(get("/palantir/").with(authentication(authentication))).andExpect(status().isOk());
        mvc.perform(get("/palantir").with(authentication(authentication))).andExpect(status().isOk());

    }

    @Test
    void verifySession() throws Throwable {
        val authentication = new UsernamePasswordAuthenticationToken(
            "casuser",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        val session = new MockHttpSession();
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.createEmptyContext()
        );

        mvc.perform(get("/palantir/dashboard/session")
            .with(authentication(authentication))
            .session(session)).andExpect(status().isOk());
        SecurityContextHolder.clearContext();
        mvc.perform(get("/palantir/dashboard/session"))
            .andExpect(status().is3xxRedirection());
    }
}
