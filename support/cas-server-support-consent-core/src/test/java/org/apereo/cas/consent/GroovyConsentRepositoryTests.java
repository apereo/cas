package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovyConsentRepositoryTests {
    private final ClassPathResource groovyResource = new ClassPathResource("ConsentRepository.groovy");

    @Test
    public void verifyConsentDecisionIsDeleted() {
        val repo = new GroovyConsentRepository(groovyResource);
        val b = repo.deleteConsentDecision(1, "CasUser");
        assertTrue(b);
    }

    @Test
    public void verifyConsentDecisionStored() {
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));

        val repo = new GroovyConsentRepository(groovyResource);
        assertTrue(repo.storeConsentDecision(decision));

        assertTrue(repo.getConsentDecisions().size() == 1);
    }
}
