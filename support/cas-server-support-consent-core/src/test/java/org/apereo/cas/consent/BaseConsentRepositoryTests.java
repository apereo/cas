package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Getter
@DirtiesContext
public abstract class BaseConsentRepositoryTests {
    protected static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());

    protected static final Service SVC = RegisteredServiceTestUtils.getService();

    protected static final AbstractRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());

    protected static final Map<String, List<Object>> ATTR = CollectionUtils.wrap("attribute", List.of("value"));

    protected static final String CASUSER_2 = "casuser2";

    public abstract ConsentRepository getRepository();

    public ConsentRepository getRepository(final String testName) {
        return getRepository();
    }

    @Test
    public void verifyConsentDecisionIsNotFound() {
        val repo = getRepository("verifyConsentDecisionIsNotFound");
        val decision = BUILDER.build(SVC, REG_SVC, "casuser", ATTR);
        decision.setId(1);
        assertNotNull(repo.storeConsentDecision(decision));
        assertFalse(repo.findConsentDecisions().isEmpty());
        assertFalse(repo.findConsentDecisions("casuser").isEmpty());
        assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication()));
        assertFalse(repo.deleteConsentDecision(decision.getId(), UUID.randomUUID().toString()));
    }

    @Test
    public void verifyConsentDecisionIsFound() {
        val repo = getRepository("verifyConsentDecisionIsFound");
        var decision = BUILDER.build(SVC, REG_SVC, CASUSER_2, ATTR);
        decision.setId(100);
        decision = repo.storeConsentDecision(decision);
        assertNotNull(decision);
        /*
         * Update the decision now that its record is created.
         */
        decision = repo.storeConsentDecision(decision);
        assertNotNull(decision);

        val d = repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(CASUSER_2));
        assertNotNull(d);
        assertEquals(CASUSER_2, d.getPrincipal());

        assertTrue(repo.deleteConsentDecision(d.getId(), d.getPrincipal()));
        assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(CASUSER_2)));
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasConsentCoreConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    static class SharedTestConfiguration {
    }
}
