package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.ticket.InvalidProxyGrantingTicketForServiceTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.apereo.cas.validation.UnauthorizedServiceTicketValidationException;

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

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasWebflowAbstractTicketExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class DefaultCasWebflowAbstractTicketExceptionHandlerTests {
    private CasWebflowExceptionHandler handler;

    private RequestContext context;

    @BeforeEach
    public void setup() {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(InvalidTicketException.class);
        errors.add(InvalidProxyGrantingTicketForServiceTicketException.class);
        errors.add(UnauthorizedServiceTicketValidationException.class);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        this.context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        this.handler = new DefaultCasWebflowAbstractTicketExceptionHandler(errors,
            MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @Test
    public void verifyUnauthz() {
        val ex = new InvalidProxyGrantingTicketForServiceTicketException(CoreAuthenticationTestUtils.getService());
        assertTrue(handler.supports(ex, context));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(InvalidProxyGrantingTicketForServiceTicketException.class.getSimpleName(), event.getId());
    }

    @Test
    public void verifyUnknown() {
        val ex = new UnrecognizableServiceForServiceTicketValidationException(CoreAuthenticationTestUtils.getService());
        assertTrue(handler.supports(ex, context));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, event.getId());
    }

}
