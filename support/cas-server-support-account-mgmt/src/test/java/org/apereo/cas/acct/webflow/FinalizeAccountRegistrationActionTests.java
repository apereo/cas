package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockParameterMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FinalizeAccountRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("WebflowActions")
@Import({
    CasAccountManagementWebflowConfiguration.class,
    FinalizeAccountRegistrationActionTests.FinalizeAccountRegistrationActionTestConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
public class FinalizeAccountRegistrationActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST)
    private Action finalizeAccountRegistrationAction;

    private RequestContext context;

    @BeforeEach
    public void setup() {
        this.context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
    }

    @Test
    public void verifyOperationFailsWithMissingRequest() throws Exception {
        val results = finalizeAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, results.getId());
    }

    @Test
    public void verifyOperationPasses() throws Exception {
        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);
        val results = finalizeAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }

    @TestConfiguration("FinalizeAccountRegistrationActionTestConfiguration")
    public static class FinalizeAccountRegistrationActionTestConfiguration {
        @Bean
        public AccountRegistrationProvisioner accountMgmtRegistrationProvisioner() throws Exception {
            val response = new AccountRegistrationResponse();
            response.putProperty("success", true);
            val provisioner = mock(AccountRegistrationProvisioner.class);
            when(provisioner.provision(any())).thenReturn(response);
            return provisioner;
        }
    }
}
