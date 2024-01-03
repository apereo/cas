package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasBasicAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasBasicAuthenticationAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class
})
@Tag("WebflowAuthenticationActions")
class BasicAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_BASIC_AUTHENTICATION)
    private Action basicAuthenticationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        HttpUtils.createBasicAuthHeaders("casuser", StringUtils.reverse("casuser")).forEach(context::addHeader);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://google.com"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.basicAuthenticationAction.execute(context).getId());
    }
}
