package org.apereo.cas.acct.webflow;

import module java.base;
import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DisplayAccountRegistrationCompletedActionTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("WebflowAccountActions")
@ImportAutoConfiguration(CasAccountManagementWebflowAutoConfiguration.class)
class DisplayAccountRegistrationCompletedActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DISPLAY_ACCOUNT_REGISTRATION_COMPLETED)
    private Action action;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    private RequestContext context;

    @BeforeEach
    void setup() throws Exception {
        this.context = MockRequestContext.create(applicationContext);
    }

    @Test
    void verifyDisplayAction() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(registeredService);

        val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
        registrationRequest.putProperty("service", service.getId());
        registrationRequest.putProperty("registeredServiceNumericId", registeredService.getId());
        registrationRequest.putProperty("fullRequestUrl", service.getOriginalUrl());
        AccountRegistrationUtils.putAccountRegistrationRequest(context, registrationRequest);
        val result = action.execute(context);
        assertNull(result);
        assertNotNull(WebUtils.getService(context));
        assertNotNull(WebUtils.getRegisteredService(context));
        assertTrue(context.getFlowScope().contains("fullRequestUrl", String.class));
    }
}
