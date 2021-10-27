package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AuthenticationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.binding.message.MessageContext;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class AuthenticationExceptionHandlerTests {
 
    @Test
    public void handleAccountNotFoundExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("notFound", AccountNotFoundException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, AccountNotFoundException.class.getSimpleName());
    }

    @Test
    public void handleUnknownExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("unknown", GeneralSecurityException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, "UNKNOWN");
    }
    
}
