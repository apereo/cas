package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Token;
import com.authy.api.Tokens;
import com.authy.api.User;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthyAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("MFA")
public class AuthyAuthenticationHandlerTests {
    @Test
    public void verifyOperation() throws Exception {
        val authyInstance = mock(AuthyClientInstance.class);
        val tokens = mock(Tokens.class);
        val token = new Token(200, "OK", "Token is valid.");
        when(tokens.verify(eq(123456), eq("token"), anyMap())).thenReturn(token);

        when(authyInstance.getAuthyTokens()).thenReturn(tokens);
        val user = new User(200, "token");
        user.setId(123456);
        when(authyInstance.getOrCreateUser(any(Principal.class))).thenReturn(user);
        val handler = new AuthyAuthenticationHandler("Authy", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(),
            authyInstance, true, 0);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

        val result = handler.authenticate(new AuthyTokenCredential("token"));
        assertNotNull(result);
    }
}
