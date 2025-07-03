package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FinalizeAccountRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("WebflowAccountActions")
@Import(FinalizeAccountRegistrationActionTests.FinalizeAccountRegistrationActionTestConfiguration.class)
@ImportAutoConfiguration(CasAccountManagementWebflowAutoConfiguration.class)
class FinalizeAccountRegistrationActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST)
    private Action finalizeAccountRegistrationAction;

    private RequestContext context;

    @BeforeEach
    void setup() throws Exception {
        this.context = MockRequestContext.create(applicationContext);
    }

    @Test
    void verifyOperationFailsWithMissingRequest() throws Throwable {
        val results = finalizeAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, results.getId());
    }

    @Test
    void verifyOperationPasses() throws Throwable {
        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);
        val results = finalizeAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }

    @TestConfiguration(value = "FinalizeAccountRegistrationActionTestConfiguration", proxyBeanMethods = false)
    static class FinalizeAccountRegistrationActionTestConfiguration {
        @Bean
        public AccountRegistrationProvisioner accountMgmtRegistrationProvisioner() throws Throwable {
            val response = new AccountRegistrationResponse();
            response.putProperty("success", true);
            val provisioner = mock(AccountRegistrationProvisioner.class);
            when(provisioner.provision(any())).thenReturn(response);
            return provisioner;
        }
    }
}
