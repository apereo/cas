package org.apereo.cas.otp.web.flow;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAccountConfirmSelectionRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
class OneTimeTokenAccountConfirmSelectionRegistrationActionTests {

    private static OneTimeTokenAccount newAccount(final String username) {
        return OneTimeTokenAccount.builder()
            .id(RandomUtils.nextLong(1, Long.MAX_VALUE))
            .username(username)
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
    }

    private static OneTimeTokenCredentialRepository newRepository(final OneTimeTokenAccount... accounts) {
        val repository = mock(OneTimeTokenCredentialRepository.class);
        for (val account : accounts) {
            when(repository.get(account.getUsername(), account.getId())).thenReturn(account);
            when(repository.get(account.getId())).thenReturn(account);
        }
        when(repository.getForCurrentTenant(anyString(), anyLong())).thenCallRealMethod();
        return repository;
    }

    @Test
    void verifyOperation() throws Throwable {
        val account = newAccount("casuser");
        val repository = newRepository(account);
        val action = new OneTimeTokenAccountConfirmSelectionRegistrationAction(repository);
        val context = MockRequestContext.create();
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(context, List.of(account, newAccount("casuser")));
        context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, account.getFormattedId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertEquals(account.getId(), MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccount(context, OneTimeTokenAccount.class).getId());
    }

    @Test
    void verifyDeviceNotOfferedIsRefused() throws Throwable {
        val account = newAccount("casuser");
        val otherAccount = newAccount("otheruser");
        val repository = newRepository(account, otherAccount);
        val action = new OneTimeTokenAccountConfirmSelectionRegistrationAction(repository);

        val context = MockRequestContext.create();
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(context, List.of(account));
        for (val id : List.of(otherAccount.getFormattedId(), "not-a-number", StringUtils.EMPTY)) {
            context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, id);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
            assertNull(MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccount(context, OneTimeTokenAccount.class));
        }

        val noDevicesOffered = MockRequestContext.create();
        noDevicesOffered.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, account.getFormattedId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(noDevicesOffered).getId());
        verify(repository, never()).get(anyLong());
        verify(repository, never()).get(otherAccount.getUsername(), otherAccount.getId());
    }

    @Test
    void verifyOfferedDeviceOfAnotherTenantIsRefused() throws Throwable {
        val account = newAccount("casuser");
        account.setTenant("shire");
        val repository = newRepository(account);
        val action = new OneTimeTokenAccountConfirmSelectionRegistrationAction(repository);
        val context = MockRequestContext.create();
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(context, List.of(account));
        context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, account.getFormattedId());
        val previousClientInfo = ClientInfoHolder.getClientInfo();
        try {
            context.setClientInfo();
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
            ClientInfoHolder.getClientInfo().setTenant("shire");
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        } finally {
            ClientInfoHolder.setClientInfo(previousClientInfo);
        }
    }
}
