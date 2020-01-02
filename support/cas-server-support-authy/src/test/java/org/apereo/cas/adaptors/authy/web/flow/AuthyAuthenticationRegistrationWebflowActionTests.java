package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Hash;
import com.authy.api.Token;
import com.authy.api.Tokens;
import com.authy.api.User;
import com.authy.api.Users;
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
 * This is {@link AuthyAuthenticationRegistrationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Webflow")
public class AuthyAuthenticationRegistrationWebflowActionTests {
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

        val hash = new Hash(200, "OK");
        hash.setSuccess(true);
        hash.setUser(user);

        val users = mock(Users.class);
        when(users.requestSms(anyInt())).thenReturn(hash);
        when(authyInstance.getAuthyUsers()).thenReturn(users);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val action = new AuthyAuthenticationRegistrationWebflowAction(authyInstance);
        val event = action.doExecute(context);
        assertEquals(CasWebflowConstants.STATE_ID_SUCCESS, event.getId());
    }
}
