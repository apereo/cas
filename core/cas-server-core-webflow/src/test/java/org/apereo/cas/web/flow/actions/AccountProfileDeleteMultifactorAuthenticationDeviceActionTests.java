package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountProfileDeleteMultifactorAuthenticationDeviceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("WebflowAuthenticationActions")
@Getter
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
class AccountProfileDeleteMultifactorAuthenticationDeviceActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_DELETE_MFA_DEVICE)
    private Action action;

    @Test
    void verifyOperation() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        requestContext.setParameter("id", "123456");
        requestContext.setParameter("source", "TestMfaProvider");
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(requestContext).getId());
    }
}
