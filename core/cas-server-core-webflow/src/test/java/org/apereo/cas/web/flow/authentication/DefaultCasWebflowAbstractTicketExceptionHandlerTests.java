package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.InvalidProxyGrantingTicketForServiceTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.validation.UnauthorizedServiceTicketValidationException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasWebflowAbstractTicketExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
class DefaultCasWebflowAbstractTicketExceptionHandlerTests {
    private CasWebflowExceptionHandler handler;

    private RequestContext context;

    @BeforeEach
    void setup() throws Exception {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(InvalidTicketException.class);
        errors.add(InvalidProxyGrantingTicketForServiceTicketException.class);
        errors.add(UnauthorizedServiceTicketValidationException.class);
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(errors);

        this.context = MockRequestContext.create();

        this.handler = new DefaultCasWebflowAbstractTicketExceptionHandler(catalog);
    }

    @Test
    void verifyUnauthz() throws Throwable {
        val ex = new InvalidProxyGrantingTicketForServiceTicketException(CoreAuthenticationTestUtils.getService());
        assertTrue(handler.supports(ex, context));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(InvalidProxyGrantingTicketForServiceTicketException.class.getSimpleName(), event.getId());
    }

    @Test
    void verifyUnknown() throws Throwable {
        val ex = new UnrecognizableServiceForServiceTicketValidationException(CoreAuthenticationTestUtils.getService());
        assertTrue(handler.supports(ex, context));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, event.getId());
    }

}
