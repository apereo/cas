package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link BaseConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseConsentRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    public abstract ConsentRepository getRepository();

    @Test
    public void verifyConsentDecisionStored() {
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        val repo = getRepository();
        assertTrue(repo.storeConsentDecision(decision));

        assertEquals(1, repo.findConsentDecisions().size());
        assertTrue(repo.deleteConsentDecision(decision.getId(), "casuser"));
    }
}
