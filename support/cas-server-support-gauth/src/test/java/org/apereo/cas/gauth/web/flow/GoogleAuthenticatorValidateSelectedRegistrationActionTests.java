package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorValidateSelectedRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class)
class GoogleAuthenticatorValidateSelectedRegistrationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_SELECTED_REGISTRATION)
    private Action googleValidateSelectedRegistrationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleValidateSelectedRegistrationAction.execute(context).getId());

        val acct = OneTimeTokenAccount.builder()
            .username(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(context, acct);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleValidateSelectedRegistrationAction.execute(context).getId());

        WebUtils.putCredential(context, new GoogleAuthenticatorTokenCredential("token", 987655L));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleValidateSelectedRegistrationAction.execute(context).getId());

        WebUtils.putCredential(context, new GoogleAuthenticatorTokenCredential("token", acct.getId()));
        assertNull(googleValidateSelectedRegistrationAction.execute(context));
    }

    @Test
    void verifyOperationWithTrustedDevice() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint(UUID.randomUUID().toString());
        record.setName("DeviceName");
        record.setPrincipal(UUID.randomUUID().toString());
        record.setId(1000);
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, record);
        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(context);
        val event = googleValidateSelectedRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertEquals(record, event.getAttributes().get("result"));
    }
}
