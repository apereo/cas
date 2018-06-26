package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.Assert.*;

/**
 * This is {@link JsonConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class JsonConsentRepositoryTests {
    private static final FileSystemResource JSON_FILE = new FileSystemResource("ConsentRepository.json");

    @AfterClass
    public static void shutdown() {
        JSON_FILE.getFile().delete();
    }

    @Test
    public void verifyConsentDecisionStored() {
        final var builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        final var regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final var svc = RegisteredServiceTestUtils.getService();
        final var decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        final var repo = new JsonConsentRepository(JSON_FILE);
        assertTrue(repo.storeConsentDecision(decision));

        assertTrue(repo.getConsentDecisions().size() == 1);
        final var b = repo.deleteConsentDecision(decision.getId(), "casuser");
        assertTrue(b);
    }
}
