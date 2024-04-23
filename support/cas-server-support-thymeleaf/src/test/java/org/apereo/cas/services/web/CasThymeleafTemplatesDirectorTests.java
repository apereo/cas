package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class CasThymeleafTemplatesDirectorTests {
    @Test
    void verifyOperation() throws Throwable {
        MockRequestContext.create();

        val plan = mock(CasWebflowExecutionPlan.class);
        when(plan.getWebflowLoginContextProviders()).thenReturn(List.of());

        val director = new CasThymeleafTemplatesDirector(plan);
        assertNotNull(director.getExceptionClassSimpleName(new AuthenticationException()));
        assertNotNull(director.getUrlExternalForm(new URI(RegisteredServiceTestUtils.CONST_TEST_URL).toURL()));
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
