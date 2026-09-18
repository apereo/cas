package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultConsentDecisionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Tag("Consent")
@ExtendWith(CasTestExtension.class)
class DefaultConsentDecisionBuilderTests {
    @Autowired
    @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    void verifyUnableToDecodeConsentDecision() {
        val consentDecision = mock(ConsentDecision.class);
        when(consentDecision.getAttributes()).thenCallRealMethod();
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        assertTrue(builder.getConsentableAttributesFrom(consentDecision).isEmpty());
    }

    @Test
    void verifyNewConsentDecision() {
        val consentDecision = getConsentDecision();
        assertNotNull(consentDecision);
        assertEquals("casuser", consentDecision.getPrincipal());
        assertEquals(consentDecision.getService(), RegisteredServiceTestUtils.getService().getId());
        assertTrue(consentDecision.getId() > 0);
        assertNotEquals(consentDecision.getId(), getConsentDecision().getId());
        assertTrue(Duration.between(consentDecision.getCreatedDate(),
            LocalDateTime.now(ZoneOffset.UTC)).abs().toMinutes() < 1);
    }

    @Test
    void verifyBadDecision() {
        val consentDecision = new ConsentDecision();
        consentDecision.setPrincipal("casuser");
        consentDecision.setService(RegisteredServiceTestUtils.getService().getId());
        assertTrue(consentDecisionBuilder.getConsentableAttributesFrom(consentDecision).isEmpty());
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision,
            CollectionUtils.wrap("attr1", List.of("value1"))));
        assertThrows(IllegalArgumentException.class,
            () -> consentDecisionBuilder.update(consentDecision, null));
    }

    @Test
    void verifyDecisionRecordedWithOtherKeys() {
        val decision = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString())
            .build(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService("test"),
                "casuser", CollectionUtils.wrap("attr1", List.of("value1")));

        val otherKeys = new DefaultConsentDecisionBuilder(
            new AttributeReleaseConsentCipherExecutor(null, null, "A128CBC-HS256", 512, 256));
        assertTrue(otherKeys.getConsentableAttributesFrom(decision).isEmpty());
        assertTrue(otherKeys.doesAttributeReleaseRequireConsent(decision,
            CollectionUtils.wrap("attr1", List.of("value1"))));
    }

    @Test
    void verifyAttributesRequireConsent() {
        val consentDecision = getConsentDecision();
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision,
            CollectionUtils.wrap("attr2", List.of("value2"))));
        assertFalse(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision,
            CollectionUtils.wrap("attr1", List.of("something"))));
    }

    @Test
    void verifyAttributeValuesRequireConsent() {
        val consentDecision = getConsentDecision();
        consentDecision.setOptions(ConsentReminderOptions.ATTRIBUTE_VALUE);
        assertTrue(consentDecisionBuilder.doesAttributeReleaseRequireConsent(consentDecision,
            CollectionUtils.wrap("attr1", List.of("value2"))));
    }

    @Test
    void verifyAttributesAreRetrieved() {
        val consentDecision = getConsentDecision();
        val attrs = consentDecisionBuilder.getConsentableAttributesFrom(consentDecision);
        assertTrue(attrs.containsKey("attr1"));
        assertEquals("value1", attrs.get("attr1").getFirst());
    }

    private ConsentDecision getConsentDecision() {
        return consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("test"),
            "casuser", CollectionUtils.wrap("attr1", List.of("value1")));
    }

}
