package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorValidateSelectedRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
class GoogleAuthenticatorValidateSelectedRegistrationActionTests {

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val action = new GoogleAuthenticatorValidateSelectedRegistrationAction();
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());

        val acct = OneTimeTokenAccount.builder()
            .username(UUID.randomUUID().toString())
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
