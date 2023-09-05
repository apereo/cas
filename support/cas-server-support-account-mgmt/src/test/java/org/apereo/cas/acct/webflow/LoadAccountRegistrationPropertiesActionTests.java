package org.apereo.cas.acct.webflow;

import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoadAccountRegistrationPropertiesActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("WebflowAccountActions")
@Import(CasAccountManagementWebflowConfiguration.class)
class LoadAccountRegistrationPropertiesActionTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_LOAD_ACCOUNT_REGISTRATION_PROPERTIES)
    private Action loadAccountRegistrationPropertiesAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        assertNull(loadAccountRegistrationPropertiesAction.execute(context));
        assertTrue(context.getFlowScope().contains("registrationProperties"));
    }
}
