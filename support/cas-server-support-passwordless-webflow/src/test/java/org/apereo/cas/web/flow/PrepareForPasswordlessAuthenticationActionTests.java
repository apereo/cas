package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.services.DefaultRegisteredServicePasswordlessPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareForPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
class PrepareForPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN)
    private Action prepareLoginAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID, prepareLoginAction.execute(context).getId());

        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertNull(prepareLoginAction.execute(context));
    }

    @Test
    void verifyFlowSkipped() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        service.setPasswordlessPolicy(new DefaultRegisteredServicePasswordlessPolicy().setEnabled(false));
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_SKIP, prepareLoginAction.execute(context).getId());
    }
}
