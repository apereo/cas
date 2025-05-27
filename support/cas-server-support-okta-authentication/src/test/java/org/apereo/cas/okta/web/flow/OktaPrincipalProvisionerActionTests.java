package org.apereo.cas.okta.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasOktaAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import com.okta.sdk.client.ClientBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OktaPrincipalProvisionerActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ImportAutoConfiguration({
    CasPersonDirectoryAutoConfiguration.class,
    CasOktaAuthenticationAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.okta.provisioning.enabled=true",
    "cas.authn.okta.provisioning.api-token=1234567890",
    "cas.authn.okta.provisioning.organization-url=http://localhost:9125"
})
@Tag("WebflowActions")
@Execution(ExecutionMode.SAME_THREAD)
class OktaPrincipalProvisionerActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_OKTA_PRINCIPAL_PROVISIONER_ACTION)
    private Action oktaPrincipalProvisionerAction;

    static {
        System.setProperty(ClientBuilder.DEFAULT_CLIENT_TESTING_DISABLE_HTTPS_CHECK_PROPERTY_NAME, "true");
    }

    @Test
    void verifyCreateOperation() throws Throwable {
        try (val webServer = new MockWebServer(9125, "[]", HttpStatus.OK)) {
            webServer.start();
            val context = MockRequestContext.create(applicationContext);
            val username = RandomUtils.randomAlphabetic(8);
            val principal = RegisteredServiceTestUtils.getPrincipal(username,
                Map.of("email", List.of("example@google.com"), "firstName", List.of("CAS")));
            val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, oktaPrincipalProvisionerAction.execute(context).getId());
        }
    }

    @Test
    void verifyUpdateOperation() throws Throwable {
        try (val webServer = new MockWebServer(9125, new ClassPathResource("okta-user.json"), HttpStatus.OK)) {
            webServer.start();
            val context = MockRequestContext.create(applicationContext);
            val username = RandomUtils.randomAlphabetic(8);
            val principal = RegisteredServiceTestUtils.getPrincipal(username,
                Map.of("email", List.of("example@google.com"), "firstName", List.of("CAS")));
            val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, oktaPrincipalProvisionerAction.execute(context).getId());
        }
    }
}

