package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
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
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationRemoveTrustedDeviceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
class MultifactorAuthenticationRemoveTrustedDeviceActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
    protected Action mfaSetTrustAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_MFA_TRUSTED_DEVICE)
    private Action mfaRemoveAction;
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = getMockRequestContext();
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        val result = mfaSetTrustAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        val record = result.getAttributes().get("result", MultifactorAuthenticationTrustRecord.class);
        context.setParameter("key", record.getRecordKey());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaRemoveAction.execute(context).getId());
    }

    private MockRequestContext getMockRequestContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("123.456.789.123");
        context.setLocalAddr("123.456.789.123");
        context.withUserAgent();
        context.setClientInfo();
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authn, context);
        return context;
    }
}
