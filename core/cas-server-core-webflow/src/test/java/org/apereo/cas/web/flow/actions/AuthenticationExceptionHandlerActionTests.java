package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.CollectionUtils;
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
import org.mockito.ArgumentCaptor;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("WebflowAuthenticationActions")
public class AuthenticationExceptionHandlerActionTests {

    private static RequestContext getMockRequestContext() {
        val ctx = mock(RequestContext.class);
        when(ctx.getMessageContext()).thenReturn(mock(MessageContext.class));
        return ctx;
    }

    private static List<CasWebflowExceptionHandler> getExceptionHandlers(final CasWebflowExceptionCatalog errors) {
        return List.of(new DefaultCasWebflowAuthenticationExceptionHandler(errors,
                MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE),
            new DefaultCasWebflowAbstractTicketExceptionHandler(errors,
                MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE),
            new GenericCasWebflowExceptionHandler(errors,
                MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE));
    }

    @Test
    public void handleAccountNotFoundExceptionByDefault() {
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(CollectionUtils.wrapSet(AccountLockedException.class, AccountNotFoundException.class));

        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(catalog));
        val req = getMockRequestContext();

        val map = new HashMap<String, Throwable>();
        map.put("notFound", new AccountNotFoundException());
        val id = handler.handle(new AuthenticationException(map), req);
        assertEquals(AccountNotFoundException.class.getSimpleName(), id);
    }

    @Test
    public void handleUnknownExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = getMockRequestContext();
        val map = new HashMap<String, Throwable>();
        map.put("unknown", new GeneralSecurityException());
        val id = handler.handle(new AuthenticationException(map), req);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, id);
    }

    @Test
    public void handleExceptions() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = getMockRequestContext();
        val event = new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR,
            new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, new GeneralSecurityException()));
        when(req.getCurrentEvent()).thenReturn(event);
        val id = handler.execute(req);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, id.getId());
    }

    @Test
    public void handleDefaultError() throws Exception {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = getMockRequestContext();
        val event = new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
        when(req.getCurrentEvent()).thenReturn(event);
        val id = handler.execute(req);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, id.getId());
    }

    @Test
    public void handleUnknownTicketExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new DefaultCasWebflowExceptionCatalog()));
        val req = getMockRequestContext();
        val id = handler.handle(new InvalidTicketException("TGT"), req);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, id);
    }

    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(CollectionUtils.wrapSet(UnsatisfiedAuthenticationPolicyException.class, AccountNotFoundException.class));
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(catalog));
        val req = getMockRequestContext();

        val policy = new TestContextualAuthenticationPolicy();
        val id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), req);
        assertEquals(UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), id);
        val message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
        verify(req.getMessageContext(), times(1)).addMessage(message.capture());
        assertArrayEquals(new String[]{policy.getCode().get()}, message.getValue().getCodes());
    }

    private static class TestContextualAuthenticationPolicy implements ContextualAuthenticationPolicy<Object> {
        @Override
        public Optional<String> getCode() {
            return Optional.of("CUSTOM_CODE");
        }

        @Override
        public Object getContext() {
            return null;
        }

        @Override
        public boolean isSatisfiedBy(final Authentication authentication) {
            return false;
        }
    }
}
