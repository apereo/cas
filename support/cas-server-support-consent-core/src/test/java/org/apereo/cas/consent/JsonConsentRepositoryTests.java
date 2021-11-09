package org.apereo.cas.consent;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class,
    properties = "cas.consent.json.location=classpath:/ConsentRepository.json")
@Getter
@Tag("FileSystem")
public class JsonConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;

    @Test
    public void verifyConsentDecisionId() {
        val user = UUID.randomUUID().toString();
        val repo = getRepository("verifyConsentDecisionId");
        val decision = repo.storeConsentDecision(BUILDER.build(SVC, REG_SVC, user, ATTR));
        assertNotNull(decision);
        assertTrue(decision.getId() > 0);
        assertTrue(repo.findConsentDecisions(user).stream().anyMatch(c -> c.getId() == decision.getId()));
    }
}
