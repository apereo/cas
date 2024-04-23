package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.actions.composite.MultifactorProviderSelectionCriteria;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustProviderSelectionCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    AbstractMultifactorAuthenticationTrustStorageTests.TestMultifactorProviderTestConfiguration.class,
    AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class
})
@Tag("WebflowMfaConfig")
@Execution(ExecutionMode.SAME_THREAD)
public class MultifactorAuthenticationTrustProviderSelectionCriteriaTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustProviderSelectionCriteria")
    private MultifactorProviderSelectionCriteria mfaTrustProviderSelectionCriteria;

    @Autowired
    @Qualifier("dummyProvider")
    private MultifactorAuthenticationProvider dummyProvider;

    @Test
    void verifyProceedWithoutTrustedDevices() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), requestContext);
        val shouldProceed = mfaTrustProviderSelectionCriteria.shouldProceedWithMultifactorProviderSelection(requestContext);
        assertTrue(shouldProceed);
    }

    @Test
    void verifyDoNotProceedWithTrustedDevices() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val attributes = new LocalAttributeMap<>();
        attributes.put(MultifactorAuthenticationProvider.class.getName(), dummyProvider);
        requestContext.setCurrentEvent(new Event(this, "eventId", attributes));
        val principal = UUID.randomUUID().toString();
        val deviceFingerprint = deviceFingerprintStrategy.determineFingerprintComponent(principal,
            requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
        var record = getMultifactorAuthenticationTrustRecord();
        record.setPrincipal(principal);
        record.setDeviceFingerprint(deviceFingerprint);
        record.setMultifactorAuthenticationProvider(dummyProvider.getId());
        record = getMfaTrustEngine().save(record);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(record.getPrincipal()), requestContext);
        requestContext.getHttpServletRequest().setCookies(requestContext.getHttpServletResponse().getCookies());
        val shouldProceed = mfaTrustProviderSelectionCriteria.shouldProceedWithMultifactorProviderSelection(requestContext);
        assertFalse(shouldProceed);
        assertTrue(requestContext.getFlashScope().contains("mfaProvider"));
    }

    @Test
    void verifyMustProceedMismatchDeviceFingerprint() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val attributes = new LocalAttributeMap<>();
        attributes.put(MultifactorAuthenticationProvider.class.getName(), dummyProvider);
        requestContext.setCurrentEvent(new Event(this, "eventId", attributes));
        val principal = UUID.randomUUID().toString();
        val deviceFingerprint = deviceFingerprintStrategy.determineFingerprintComponent(principal,
            requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());
        var record = getMultifactorAuthenticationTrustRecord();
        record.setPrincipal(principal);
        record.setDeviceFingerprint(deviceFingerprint);
        record.setMultifactorAuthenticationProvider(UUID.randomUUID().toString());
        record = getMfaTrustEngine().save(record);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(record.getPrincipal()), requestContext);
        requestContext.getHttpServletRequest().setCookies(requestContext.getHttpServletResponse().getCookies());
        val shouldProceed = mfaTrustProviderSelectionCriteria.shouldProceedWithMultifactorProviderSelection(requestContext);
        assertTrue(shouldProceed);
    }
}
