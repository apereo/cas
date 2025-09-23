package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.DenyAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.registry.ClosedYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountCheckRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowMfaActions")
class YubiKeyAccountCheckRegistrationActionTests extends BaseYubiKeyActionTests {

    @Test
    void verifyActionSuccess() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, new YubiKeyMultifactorAuthenticationProvider());
        val action = new YubiKeyAccountCheckRegistrationAction(
            new OpenYubiKeyAccountRegistry(new AcceptAllYubiKeyAccountValidator()), tenantExtractor);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyActionRegister() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, new YubiKeyMultifactorAuthenticationProvider());
        val registry = new ClosedYubiKeyAccountRegistry(new DenyAllYubiKeyAccountValidator());
        val action = new YubiKeyAccountCheckRegistrationAction(registry, tenantExtractor);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, action.execute(context).getId());

    }
}
