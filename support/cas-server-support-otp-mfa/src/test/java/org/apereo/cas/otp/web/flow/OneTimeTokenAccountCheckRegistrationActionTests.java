package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAccountCheckRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class OneTimeTokenAccountCheckRegistrationActionTests {
    @Test
    void verifyExistingAccount() throws Throwable {
        val account = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
        val repository = mock(OneTimeTokenCredentialRepository.class);
        when(repository.get(anyString())).thenReturn((Collection) List.of(account));
        val action = new OneTimeTokenAccountCheckRegistrationAction(repository);

        val context = MockRequestContext.create();
        ApplicationContextProvider.registerBeanIntoApplicationContext(context.getApplicationContext(),
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(context.getApplicationContext());
        WebUtils.putMultifactorAuthenticationProvider(context, provider);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyCreateAccount() throws Throwable {
        val account = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
        val repository = mock(OneTimeTokenCredentialRepository.class);
        when(repository.create(anyString())).thenReturn(account);
        val action = new OneTimeTokenAccountCheckRegistrationAction(repository);

        val context = MockRequestContext.create();
        ApplicationContextProvider.registerBeanIntoApplicationContext(context.getApplicationContext(),
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(context.getApplicationContext());
        WebUtils.putMultifactorAuthenticationProvider(context, provider);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, action.execute(context).getId());
    }
}
