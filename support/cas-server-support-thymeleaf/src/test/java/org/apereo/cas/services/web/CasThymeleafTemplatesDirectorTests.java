package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.thymeleaf.context.WebEngineContext;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasThymeleafTemplatesDirectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasThymeleafTemplatesDirectorTests {

    private static final String LOGIN_URL = "http://localhost:8080/cas/login";
    private static final String LOGIN_URL_WITH_CLIENT_NAME = "http://localhost:8080/cas/login?client_name=Client1";

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        MockRequestContext.create(applicationContext);

        val plan = mock(CasWebflowExecutionPlan.class);
        when(plan.getWebflowLoginContextProviders()).thenReturn(List.of());

        val director = new CasThymeleafTemplatesDirector(plan);
        assertNotNull(director.getExceptionClassSimpleName(new AuthenticationException()));
        assertEquals(LOGIN_URL + "?", director.getUrlExternalForm(new URI(LOGIN_URL).toURL()));
        assertEquals(LOGIN_URL_WITH_CLIENT_NAME + "&", director.getUrlExternalForm(new URI(LOGIN_URL_WITH_CLIENT_NAME).toURL()));
        assertTrue(director.isLoginFormViewable(mock(WebEngineContext.class)));
        assertTrue(director.isLoginFormUsernameInputVisible(mock(WebEngineContext.class)));
        assertFalse(director.isLoginFormUsernameInputDisabled(mock(WebEngineContext.class)));

        assertTrue(director.getLoginFormUsername(mock(WebEngineContext.class)).isEmpty());

        val provider = mock(CasWebflowLoginContextProvider.class);
        when(provider.getOrder()).thenCallRealMethod();
        when(provider.getCandidateUsername(any())).thenReturn(Optional.of("cas"));
        when(plan.getWebflowLoginContextProviders()).thenReturn(List.of(provider));
        assertFalse(director.getLoginFormUsername(mock(WebEngineContext.class)).isEmpty());

        assertNotNull(director.format(LocalDateTime.now(Clock.systemUTC()), "yyyy/mm/dd"));
    }
}
