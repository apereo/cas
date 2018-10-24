package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.category.MailCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link VerifySecurityQuestionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 25000)
@Category(MailCategory.class)
public class VerifySecurityQuestionsActionTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyAction() {
        try {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("q0", "securityAnswer1");
            context.getFlowScope().put("username", "casuser");
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals("success", verifySecurityQuestionsAction.execute(context).getId());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
