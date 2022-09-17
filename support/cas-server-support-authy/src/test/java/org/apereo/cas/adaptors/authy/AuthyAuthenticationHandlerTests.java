package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.support.WebUtils;

import com.authy.AuthyApiClient;
import com.authy.api.Token;
import com.authy.api.Tokens;
import com.authy.api.User;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthyAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
public class AuthyAuthenticationHandlerTests {
    private static AuthyClientInstance configureAuthyClientInstance(final int userStatus,
                                                                    final int tokenStatus, final String message) throws Exception {
        val authyInstance = mock(AuthyClientInstance.class);
        val apiClient = mock(AuthyApiClient.class);
        when(authyInstance.authyClient()).thenReturn(apiClient);

        val tokens = mock(Tokens.class);
        val token = new Token(tokenStatus, "OK", message);
        when(tokens.verify(eq(123456), eq("token"), anyMap())).thenReturn(token);

        when(apiClient.getTokens()).thenReturn(tokens);
        val user = new User(userStatus, "token");
        user.setId(123456);
        when(authyInstance.getOrCreateUser(any(Principal.class))).thenReturn(user);
        return authyInstance;
    }

    @Test
    public void verifyOperation() throws Exception {
        val authyInstance = configureAuthyClientInstance(200, 200, Token.VALID_TOKEN_MESSAGE);
        val handler = getAuthyAuthenticationHandler(authyInstance);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

        val result = handler.authenticate(new AuthyTokenCredential("token"), mock(Service.class));
        assertNotNull(result);

        assertTrue(handler.supports(new AuthyTokenCredential("token")));
        assertTrue(handler.supports(AuthyTokenCredential.class));
    }

    @Test
    public void verifyFailsOperation() throws Exception {
        val authyInstance = configureAuthyClientInstance(400, 200, Token.VALID_TOKEN_MESSAGE);
        val handler = getAuthyAuthenticationHandler(authyInstance);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);

        assertThrows(NullPointerException.class, () -> handler.authenticate(new AuthyTokenCredential("token"), mock(Service.class)));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new AuthyTokenCredential("token"), mock(Service.class)));
    }

    @Test
    public void verifyFailsVerify() throws Exception {
        val authyInstance = configureAuthyClientInstance(200, 400, "Bad");
        val handler = getAuthyAuthenticationHandler(authyInstance);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(new AuthyTokenCredential("token"), mock(Service.class)));
    }

    private static AuthyAuthenticationHandler getAuthyAuthenticationHandler(final AuthyClientInstance authyInstance) {
        return new AuthyAuthenticationHandler("Authy", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(),
            authyInstance, true, 0,
            new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class)));
    }
}
