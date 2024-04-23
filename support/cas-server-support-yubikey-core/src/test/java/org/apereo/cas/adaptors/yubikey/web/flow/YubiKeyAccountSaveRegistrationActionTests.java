package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowMfaActions")
class YubiKeyAccountSaveRegistrationActionTests extends BaseYubiKeyActionTests {
    @Test
    void verifyActionSuccess() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putMultifactorAuthenticationProvider(context, new YubiKeyMultifactorAuthenticationProvider());
        context.setParameter(YubiKeyAccountSaveRegistrationAction.PARAMETER_NAME_TOKEN, "yubikeyToken");
        context.setParameter(YubiKeyAccountSaveRegistrationAction.PARAMETER_NAME_ACCOUNT, UUID.randomUUID().toString());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val action = new YubiKeyAccountSaveRegistrationAction(new OpenYubiKeyAccountRegistry(new AcceptAllYubiKeyAccountValidator()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyActionFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putMultifactorAuthenticationProvider(context, new YubiKeyMultifactorAuthenticationProvider());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val action = new YubiKeyAccountSaveRegistrationAction(new OpenYubiKeyAccountRegistry(new AcceptAllYubiKeyAccountValidator()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }
}
