package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GenericCasWebflowExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class GenericCasWebflowExceptionHandlerTests {
    @Test
    public void verifyOperation() {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(AccountLockedException.class);
        errors.add(CredentialExpiredException.class);
        errors.add(AccountExpiredException.class);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        val handler = new GenericCasWebflowExceptionHandler(errors, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
        assertTrue(handler.supports(new AccountExpiredException(), context));

        val event = handler.handle(new CredentialExpiredException(), context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, event.getId());
    }
}
