package org.apereo.cas.services.web;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.web.support.WebUtils;

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

import java.util.UUID;

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

        val director = new CasThymeleafLoginFormDirector();
        assertTrue(director.isLoginFormViewable(mock(WebEngineContext.class)));
        assertTrue(director.isLoginFormUsernameInputVisible(mock(WebEngineContext.class)));
        assertFalse(director.isLoginFormUsernameInputDisabled(mock(WebEngineContext.class)));

        val id = UUID.randomUUID().toString();
        val account = new BasicIdentifiableCredential();
        account.setId(id);
        WebUtils.putPasswordlessAuthenticationAccount(context, account);
        assertNotNull(director.getLoginFormUsername(mock(WebEngineContext.class)));
    }
}
