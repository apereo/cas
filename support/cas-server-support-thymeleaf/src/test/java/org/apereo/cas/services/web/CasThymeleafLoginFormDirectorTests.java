package org.apereo.cas.services.web;

import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import org.thymeleaf.context.WebEngineContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasThymeleafLoginFormDirectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
public class CasThymeleafLoginFormDirectorTests {
    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val plan = mock(CasWebflowExecutionPlan.class);
        when(plan.getWebflowLoginContextProviders()).thenReturn(List.of());

        val director = new CasThymeleafLoginFormDirector(plan);
        assertTrue(director.isLoginFormViewable(mock(WebEngineContext.class)));
        assertTrue(director.isLoginFormUsernameInputVisible(mock(WebEngineContext.class)));
        assertFalse(director.isLoginFormUsernameInputDisabled(mock(WebEngineContext.class)));

        assertTrue(director.getLoginFormUsername(mock(WebEngineContext.class)).isEmpty());

        val provider = mock(CasWebflowLoginContextProvider.class);
        when(provider.getOrder()).thenCallRealMethod();
        when(provider.getCandidateUsername(any())).thenReturn(Optional.of("cas"));
        when(plan.getWebflowLoginContextProviders()).thenReturn(List.of(provider));
        assertFalse(director.getLoginFormUsername(mock(WebEngineContext.class)).isEmpty());
    }
}
