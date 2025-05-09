package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SendForgotUsernameInstructionsActionEmailMessageBodyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class SendForgotUsernameInstructionsActionEmailMessageBodyTests extends BasePasswordManagementActionTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=CAS",
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attribute-repository.stub.attributes.cn=CAS",
        "cas.authn.attribute-repository.stub.attributes.givenName=casuser",
        "cas.authn.pm.forgot-username.mail.text=classpath:ForgotUsernameEmailBody.groovy"
    })
    class DefaultTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        void verifyOp() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("email", "casuser@apereo.org");
            val result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        }

        @Test
        void verifyBodyContainsUsername() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            context.setParameter("email", "casuser@apereo.org");
            val resultEvent = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resultEvent.getId());

            val resultAttributeName = new EventFactorySupport().getResultAttributeName();
            val emailResult = resultEvent.getAttributes().get(resultAttributeName, EmailCommunicationResult.class);
            assertTrue(emailResult.isSuccess());
            assertEquals("Hello CAS with email casuser@apereo.org, your affiliation is developer", emailResult.getBody());
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled=false")
    class NoPrincipalResolutionTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        void verifyBodyContainsUsername() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            context.setParameter("email", "casuser@apereo.org");
            val resultEvent = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resultEvent.getId());

            val resultAttributeName = new EventFactorySupport().getResultAttributeName();
            val emailResult = resultEvent.getAttributes().get(resultAttributeName, EmailCommunicationResult.class);
            assertTrue(emailResult.isSuccess());
            assertEquals("Your current username is: casuser", emailResult.getBody());
        }

    }
}
