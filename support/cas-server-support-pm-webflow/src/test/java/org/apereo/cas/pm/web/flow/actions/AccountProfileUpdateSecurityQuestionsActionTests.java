package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountProfileUpdateSecurityQuestionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = {
    "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.reset.security-questions-enabled=true",
    "CasFeatureModule.AccountManagement.enabled=true"
})
@ImportAutoConfiguration(CasCoreWebflowAutoConfiguration.class)
class AccountProfileUpdateSecurityQuestionsActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS)
    private Action accountProfileUpdateSecurityQuestionsAction;

    private MockRequestContext prepareContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        ticketRegistry.addTicket(tgt);
        return context;
    }

    @Test
    void verifyMismatchedQuestionsAndAnswers() throws Throwable {
        val context = prepareContext();
        context.setParameter("questions", new String[]{"question1", "question2", "question3"});
        context.setParameter("answers", new String[]{"answer1", "answer2"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyShortQuestions() throws Throwable {
        val context = prepareContext();
        context.setParameter("questions", new String[]{"qaz", "zsxa", "123"});
        context.setParameter("answers", new String[]{"a", "b", "c"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyOperationPasses() throws Throwable {
        val context = prepareContext();
        context.setParameter("questions", new String[]{"question1", "question2"});
        context.setParameter("answers", new String[]{"answer1", "answer2"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }
}
