package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link InMemoryConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InMemoryConsentRepositoryTests {
    @Test
    public void verifyConsentDecisionStored() {
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        val repo = new InMemoryConsentRepository();
        assertTrue(repo.storeConsentDecision(decision));

        assertTrue(repo.getConsentDecisions().size() == 1);
        val b = repo.deleteConsentDecision(decision.getId(), "casuser");
        assertTrue(b);
    }
}
