package org.apereo.cas.consent;

import java.nio.file.Files;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.util.UUID;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class,
    properties = "cas.consent.json.location=file://${java.io.tmpdir}/ConsentRepository.json")
@Getter
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
class JsonConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

    @Test
    void verifyConsentDecisionId() throws Throwable {
        val user = UUID.randomUUID().toString();
        val repo = getRepository();
        val decision = repo.storeConsentDecision(BUILDER.build(SVC, REG_SVC, user, ATTR));
        assertNotNull(decision);
        assertTrue(decision.getId() > 0);
        await().untilAsserted(() -> assertTrue(repo.findConsentDecisions(user)
            .stream().anyMatch(desc -> desc.getId() == decision.getId())));
    }

    @Test
    void verifyDisposedRepository() throws Throwable {
        val repo = new JsonConsentRepository(new FileSystemResource(Files.createTempFile("records", ".json").toFile()));
        assertNotNull(repo.getWatcherService());
        assertDoesNotThrow(repo::destroy);
    }
}
