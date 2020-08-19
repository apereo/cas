package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAccountConfirmSelectionRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class OneTimeTokenAccountConfirmSelectionRegistrationActionTests {

    @Test
    public void verifyOperation() {
        val account = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
        val repository = mock(OneTimeTokenCredentialRepository.class);
        when(repository.get(anyLong())).thenReturn(account);

        val action = new OneTimeTokenAccountConfirmSelectionRegistrationAction(repository);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID,
            String.valueOf(account.getId()));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.doExecute(context).getId());
    }

}
