package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Tag("Groovy")
@TestPropertySource(properties = "cas.acceptable-usage-policy.groovy.location=classpath:/AcceptableUsagePolicy.groovy")
class GroovyAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    void verifyRepositoryActionWithAdvancedConfig() throws Throwable {
        verifyRepositoryAction("casuser", CollectionUtils.wrap("aupAccepted", "false"));
    }

    @Test
    void verifyPolicyTerms() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val tgt = new MockTicketGrantingTicket(credential.getId(), credential, Map.of());
        ticketRegistry.addTicket(tgt);

        WebUtils.putAuthentication(tgt.getAuthentication(), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(acceptableUsagePolicyRepository.fetchPolicy(context).isPresent());
    }

    @Test
    void verifyPolicyTermsFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val tgt = new MockTicketGrantingTicket(credential.getId(), credential, Map.of());
        ticketRegistry.addTicket(tgt);
        assertFalse(acceptableUsagePolicyRepository.fetchPolicy(context).isPresent());
    }
}
