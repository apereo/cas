package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("Webflow")
public class AuthenticationExceptionHandlerActionTests {

    @Test
    public void handleAccountNotFoundExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(
            CollectionUtils.wrapSet(AccountLockedException.class, AccountNotFoundException.class)));
        val req = getMockRequestContext();

        val map = new HashMap<String, Throwable>();
        map.put("notFound", new AccountNotFoundException());
        val id = handler.handle(new AuthenticationException(map), req);
        assertEquals(AccountNotFoundException.class.getSimpleName(), id);
    }

    @Test
    public void handleUnknownExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new LinkedHashSet()));
        val req = getMockRequestContext();
        val map = new HashMap<String, Throwable>();
        map.put("unknown", new GeneralSecurityException());
        val id = handler.handle(new AuthenticationException(map), req);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, id);
    }

    @Test
    public void handleUnknownTicketExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(new LinkedHashSet()));
        val req = getMockRequestContext();
        val id = handler.handle(new InvalidTicketException("TGT"), req);
        assertEquals(CasWebflowExceptionHandler.UNKNOWN, id);
    }

    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        val handler = new AuthenticationExceptionHandlerAction(getExceptionHandlers(
            CollectionUtils.wrapSet(UnsatisfiedAuthenticationPolicyException.class, AccountNotFoundException.class)));
        val req = getMockRequestContext();

        val policy = new TestContextualAuthenticationPolicy();
        val id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), req);
        assertEquals(UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), id);
        val message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
        verify(req.getMessageContext(), times(1)).addMessage(message.capture());
        assertArrayEquals(new String[]{policy.getCode().get()}, message.getValue().getCodes());
    }

    private static RequestContext getMockRequestContext() {
        val ctx = mock(RequestContext.class);
        when(ctx.getMessageContext()).thenReturn(mock(MessageContext.class));
        return ctx;
    }

    private static List<CasWebflowExceptionHandler> getExceptionHandlers(final Set errors) {
        return List.of(new DefaultCasWebflowAuthenticationExceptionHandler(errors, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE),
            new DefaultCasWebflowAbstractTicketExceptionHandler(errors, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE),
            new GenericCasWebflowExceptionHandler(errors, MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE));
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
