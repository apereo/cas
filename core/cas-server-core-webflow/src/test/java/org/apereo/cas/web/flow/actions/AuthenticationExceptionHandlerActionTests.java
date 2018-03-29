package org.apereo.cas.web.flow.actions;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class AuthenticationExceptionHandlerActionTests {
 
    @Test
    public void handleAccountNotFoundExceptionByDefault() {
        final var handler = new AuthenticationExceptionHandlerAction(
                CollectionUtils.wrapSet(AccountLockedException.class, AccountNotFoundException.class)
        );
        final var req = getMockRequestContext();

        final Map<String, Throwable> map = new HashMap<>();
        map.put("notFound", new AccountNotFoundException());
        final var id = handler.handle(new AuthenticationException(map), req);
        assertEquals(AccountNotFoundException.class.getSimpleName(), id);
    }

    private RequestContext getMockRequestContext() {
        final var ctx = mock(RequestContext.class);
        when(ctx.getMessageContext()).thenReturn(mock(MessageContext.class));
        return ctx;
    }

    @Test
    public void handleUnknownExceptionByDefault() {
        final var handler = new AuthenticationExceptionHandlerAction();
        final var req = getMockRequestContext();
        final Map<String, Throwable> map = new HashMap<>();
        map.put("unknown", new GeneralSecurityException());
        final var id = handler.handle(new AuthenticationException(map), req);
        assertEquals("UNKNOWN", id);
    }

    @Test
    public void handleUnknownTicketExceptionByDefault() {
        final var handler = new AuthenticationExceptionHandlerAction();
        final var req = getMockRequestContext();

        final var id = handler.handle(new InvalidTicketException("TGT"), req);
        assertEquals("UNKNOWN", id);
    }
    
    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        final var handler = new AuthenticationExceptionHandlerAction(
                CollectionUtils.wrapSet(UnsatisfiedAuthenticationPolicyException.class,
                        AccountNotFoundException.class)
        );
        final var req = getMockRequestContext();

        final ContextualAuthenticationPolicy<?> policy = new TestContextualAuthenticationPolicy();
        final var id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), req);
        assertEquals("UnsatisfiedAuthenticationPolicyException", id);
        final var message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
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
