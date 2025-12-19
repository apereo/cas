package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("WebflowAuthenticationActions")
class AuthenticationExceptionHandlerActionTests {


    private static List<CasWebflowExceptionHandler> getExceptionHandlers(final CasWebflowExceptionCatalog errors) {
        return List.of(new DefaultCasWebflowAuthenticationExceptionHandler(errors),
            new DefaultCasWebflowAbstractTicketExceptionHandler(errors),
            new GenericCasWebflowExceptionHandler(errors));
    }

    @Test
    void handleAccountNotFoundExceptionByDefault() throws Exception {
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(CollectionUtils.wrapSet(AccountLockedException.class, AccountNotFoundException.class));

        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(catalog));
        val req = MockRequestContext.create();

        val map = new HashMap<String, Throwable>();
        map.put("notFound", new AccountNotFoundException());
        val event = handler.handle(new AuthenticationException(map), req);
        assertEquals(AccountNotFoundException.class.getSimpleName(), event.getId());
    }

    @Test
    void handleUnknownExceptionByDefault() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = MockRequestContext.create();
        val map = new HashMap<String, Throwable>();
        map.put("unknown", new GeneralSecurityException());
        val event = handler.handle(new AuthenticationException(map), req);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, event.getId());
    }

    @Test
    void handleExceptions() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = MockRequestContext.create();
        val event = new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR,
            new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, new GeneralSecurityException()));
        req.setCurrentEvent(event);
        val id = handler.execute(req);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, id.getId());
    }

    @Test
    void handleDefaultError() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = MockRequestContext.create();
        val event = new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
        req.setCurrentEvent(event);
        val id = handler.execute(req);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, id.getId());
    }

    @Test
    void handleUnknownTicketExceptionByDefault() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = MockRequestContext.create();
        val event = handler.handle(new InvalidTicketException("TGT"), req);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, event.getId());
    }

    @Test
    void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() throws Exception {
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(CollectionUtils.wrapSet(UnsatisfiedAuthenticationPolicyException.class, AccountNotFoundException.class));
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(catalog));
        val req = MockRequestContext.create();

        val event = handler.handle(new UnsatisfiedAuthenticationPolicyException(AuthenticationPolicy.neverSatisfied()), req);
        assertEquals(UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), event.getId());
    }
}
