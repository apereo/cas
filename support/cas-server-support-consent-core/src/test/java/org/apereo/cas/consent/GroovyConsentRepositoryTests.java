package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
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
        final GroovyConsentRepository repo = new GroovyConsentRepository(groovyResource);
        final boolean b = repo.deleteConsentDecision(1, "CasUser");
        assertTrue(b);
    }

    @Test
    public void verifyConsentDecisionStored() {
        final DefaultConsentDecisionBuilder builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final ConsentDecision decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));

        final GroovyConsentRepository repo = new GroovyConsentRepository(groovyResource);
        assertTrue(repo.storeConsentDecision(decision));

        assertTrue(repo.getConsentDecisions().size() == 1);
    }
}
