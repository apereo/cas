package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleAuthenticatorValidateSelectedRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
public class GoogleAuthenticatorValidateSelectedRegistrationActionTests {

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val action = new GoogleAuthenticatorValidateSelectedRegistrationAction();
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());

        val acct = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        WebUtils.putOneTimeTokenAccount(context, acct);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());

        WebUtils.putCredential(context, new GoogleAuthenticatorTokenCredential("token", 987655L));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());

        WebUtils.putCredential(context, new GoogleAuthenticatorTokenCredential("token", acct.getId()));
        assertNull(action.execute(context));
    }
}
