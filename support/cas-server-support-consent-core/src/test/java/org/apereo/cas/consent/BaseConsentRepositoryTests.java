package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import lombok.Getter;
import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link BaseConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class
})
@Getter
public abstract class BaseConsentRepositoryTests {
    protected static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
    protected static final Service SVC = RegisteredServiceTestUtils.getService();
    protected static final AbstractRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());

    protected static final Map<String, Object> ATTR = CollectionUtils.wrap("attribute", "value");
    protected static final String CASUSER_2 = "casuser2";

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    public abstract ConsentRepository getRepository();

    public ConsentRepository getRepository(final String testName) {
        return getRepository();
    }

    @Test
    public void verifyConsentDecisionIsNotFound() {
        val repo = getRepository("verifyConsentDecisionIsNotFound");
        val decision = BUILDER.build(SVC, REG_SVC, "casuser", ATTR);
        decision.setId(1);
        repo.storeConsentDecision(decision);

        assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication()));
    }

    @Test
    public void verifyConsentDecisionIsFound() {
        val repo = getRepository("verifyConsentDecisionIsFound");
        val decision = BUILDER.build(SVC, REG_SVC, CASUSER_2, ATTR);
        decision.setId(100);
        repo.storeConsentDecision(decision);

        val d = repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(CASUSER_2));
        assertNotNull(d);
        assertEquals(CASUSER_2, d.getPrincipal());

        assertTrue(repo.deleteConsentDecision(d.getId(), d.getPrincipal()));
        assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(CASUSER_2)));
    }
}
