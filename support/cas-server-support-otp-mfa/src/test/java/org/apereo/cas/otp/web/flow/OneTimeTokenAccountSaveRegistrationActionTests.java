package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAccountSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class OneTimeTokenAccountSaveRegistrationActionTests {
    @Test
    void verifyCreateAccount() throws Throwable {
        val props = new CasConfigurationProperties();
        val account = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
        val repository = mock(OneTimeTokenCredentialRepository.class);
        val action = new OneTimeTokenAccountSaveRegistrationAction(repository, props);

        val context = MockRequestContext.create();
        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, "ExampleAccount");
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyMissingAccount() throws Throwable {
        val props = new CasConfigurationProperties();
        val repository = mock(OneTimeTokenCredentialRepository.class);
        val action = new OneTimeTokenAccountSaveRegistrationAction(repository, props);

        val context = MockRequestContext.create();
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }
}
