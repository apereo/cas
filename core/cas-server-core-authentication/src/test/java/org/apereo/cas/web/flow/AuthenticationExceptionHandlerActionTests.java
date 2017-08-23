package org.apereo.cas.web.flow;

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

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class AuthenticationExceptionHandlerActionTests {
 
    @Test
    public void handleAccountNotFoundExceptionByDefault() {
        final AuthenticationExceptionHandlerAction handler = new AuthenticationExceptionHandlerAction(
                CollectionUtils.wrapSet(AccountLockedException.class,
                        AccountNotFoundException.class)
        );
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Throwable>> map = new HashMap<>();
        map.put("notFound", AccountNotFoundException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, AccountNotFoundException.class.getSimpleName());
    }

    @Test
    public void handleUnknownExceptionByDefault() {
        final AuthenticationExceptionHandlerAction handler = new AuthenticationExceptionHandlerAction();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Throwable>> map = new HashMap<>();
        map.put("unknown", GeneralSecurityException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, "UNKNOWN");
    }

    @Test
    public void handleUnknownTicketExceptionByDefault() {
        final AuthenticationExceptionHandlerAction handler = new AuthenticationExceptionHandlerAction();
        final MessageContext ctx = mock(MessageContext.class);

        final String id = handler.handle(new InvalidTicketException("TGT"), ctx);
        assertEquals(id, "UNKNOWN");
        verifyZeroInteractions(ctx);
    }

    @Test
    public void correctHandlersOrder() {
        final AuthenticationExceptionHandlerAction handler = 
                new AuthenticationExceptionHandlerAction(CollectionUtils.wrapSet(AccountLockedException.class,
                        AccountNotFoundException.class));
        final MessageContext ctx = mock(MessageContext.class);

        final Map<String, Class<? extends Throwable>> map = new LinkedHashMap<>();
        map.put("accountLocked", AccountLockedException.class);
        map.put("accountNotFound", AccountNotFoundException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, AccountLockedException.class.getSimpleName());
    }


    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        final AuthenticationExceptionHandlerAction handler = new AuthenticationExceptionHandlerAction(
                CollectionUtils.wrapSet(UnsatisfiedAuthenticationPolicyException.class,
                        AccountNotFoundException.class)
        );
        final MessageContext ctx = mock(MessageContext.class);

        final ContextualAuthenticationPolicy<?> policy = new TestContextualAuthenticationPolicy();
        final String id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), ctx);
        assertEquals(id, "UnsatisfiedAuthenticationPolicyException");
        final ArgumentCaptor<DefaultMessageResolver> message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
        verify(ctx, times(1)).addMessage(message.capture());
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
