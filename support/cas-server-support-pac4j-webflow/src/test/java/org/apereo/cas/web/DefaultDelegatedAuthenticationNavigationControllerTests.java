package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link DefaultDelegatedAuthenticationNavigationControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@AutoConfigureMockMvc
@Tag("Delegation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultDelegatedAuthenticationNavigationControllerTests {

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyRedirectByParam() throws Throwable {
        var result = mockMvc.perform(get("/login/{clientName}", "CASClient")
                .param(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient")
                .param("customParam", "customValue"))
            .andReturn();
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertTrue(new URIBuilder(result.getResponse().getRedirectedUrl()).getQueryParams()
            .stream().anyMatch(valuePair -> valuePair.getName().equals(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)));

        result = mockMvc.perform(post("/login/{clientName}", "CASClient")
                .param(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient")
                .param("customParam", "customValue"))
            .andReturn();
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertTrue(new URIBuilder(result.getResponse().getRedirectedUrl()).getQueryParams()
            .stream().anyMatch(valuePair -> valuePair.getName().equals(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)));
    }

}
