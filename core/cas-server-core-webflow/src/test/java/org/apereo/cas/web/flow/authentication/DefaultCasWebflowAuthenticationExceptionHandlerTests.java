package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasWebflowAuthenticationExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationHandler")
class DefaultCasWebflowAuthenticationExceptionHandlerTests {
    private CasWebflowExceptionHandler handler;

    private RequestContext context;

    @BeforeEach
    void setup() throws Exception {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(UnauthorizedAuthenticationException.class);
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(errors);

        this.context = MockRequestContext.create();
        this.handler = new DefaultCasWebflowAuthenticationExceptionHandler(catalog);
    }

    @Test
    void verifyServiceUnauthz() throws Throwable {
        assertTrue(handler.supports(new UnauthorizedAuthenticationException("failure"), context));

        WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, new URI("https://github.com"));
        val ex = new AuthenticationException(new UnauthorizedServiceForPrincipalException("failure",
            CoreAuthenticationTestUtils.getRegisteredService(), "casuser", Map.of()));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, event.getId());
    }

    @Test
    void verifyUnknown() throws Throwable {
        val ex = new AuthenticationException(new InvalidLoginLocationException("failure"));
        val event = handler.handle(ex, context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, event.getId());
    }

    @Test
    void verifyAuthUnauthz() throws Throwable {
        for (var i = 0; i < 3; i++) {
            val ex = new AuthenticationException(new UnauthorizedAuthenticationException("failure"));
            val event = handler.handle(ex, context);
            assertNotNull(event);
            assertEquals(UnauthorizedAuthenticationException.class.getSimpleName(), event.getId());
        }
        val count = WebUtils.countFailedAuthenticationAttempts(context);
        assertEquals(3, count);
    }
}
