package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Getter
@Tag("Consent")
@ExtendWith(CasTestExtension.class)
class InMemoryConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

    @Test
    void verifyChainingDeletesFromAllRepositories() throws Throwable {
        val first = new InMemoryConsentRepository();
        val second = new InMemoryConsentRepository();
        val chain = new ChainingConsentRepository(List.of(first, second));
        val user = getUser();
        val decision = chain.storeConsentDecision(BUILDER.build(SVC, REG_SVC, user, ATTR));
        assertNotNull(chain.storeConsentDecision(BUILDER.build(SVC2, REG_SVC2, user, ATTR)));
        assertEquals(2, first.findConsentDecisions(user).size());
        assertEquals(2, second.findConsentDecisions(user).size());

        assertTrue(chain.deleteConsentDecision(decision.getId(), user));
        assertEquals(1, first.findConsentDecisions(user).size());
        assertEquals(1, second.findConsentDecisions(user).size());

        assertTrue(chain.deleteConsentDecisions(user));
        assertTrue(first.findConsentDecisions(user).isEmpty());
        assertTrue(second.findConsentDecisions(user).isEmpty());
        assertFalse(chain.deleteConsentDecisions(user));
    }
}
