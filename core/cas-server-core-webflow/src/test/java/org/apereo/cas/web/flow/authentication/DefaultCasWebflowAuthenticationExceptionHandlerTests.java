package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasWebflowAuthenticationExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultCasWebflowAuthenticationExceptionHandlerTests {
    private CasWebflowExceptionHandler handler;

    private RequestContext context;

    @BeforeEach
    public void setup() {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(UnauthorizedAuthenticationException.class);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        this.context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        this.handler = new DefaultCasWebflowAuthenticationExceptionHandler(errors,
            MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @Test
    public void verifyServiceUnauthz() throws Exception {
        assertTrue(handler.supports(new UnauthorizedAuthenticationException("failure"), context));

        WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, new URI("https://github.com"));
        val ex = new AuthenticationException(new UnauthorizedServiceForPrincipalException("failure",
            CoreAuthenticationTestUtils.getRegisteredService(), "casuser", Map.of()));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, event.getId());
    }

    @Test
    public void verifyUnknown() {
        val ex = new AuthenticationException(new InvalidLoginLocationException("failure"));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, event.getId());
    }

    @Test
    public void verifyAuthUnauthz() {
        val ex = new AuthenticationException(new UnauthorizedAuthenticationException("failure"));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(UnauthorizedAuthenticationException.class.getSimpleName(), event.getId());
    }
}
