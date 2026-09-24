package org.apereo.cas.okta.web.flow;

import module java.base;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
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
class OktaPrincipalProvisionerActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_OKTA_PRINCIPAL_PROVISIONER_ACTION)
    private Action oktaPrincipalProvisionerAction;

    static {
        System.setProperty(ClientBuilder.DEFAULT_CLIENT_TESTING_DISABLE_HTTPS_CHECK_PROPERTY_NAME, "true");
    }

    /**
     * The Okta organization URL below is a fixed port, so the create and the update path share one
     * server rather than competing for it: an empty user list drives the create, the canned user
     * drives the update.
     *
     * @throws Throwable in case of failure
     */
    @Test
    void verifyOperation() throws Throwable {
        try (val webServer = new MockWebServer(9125, "[]", HttpStatus.OK)) {
            webServer.start();
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, provision());
        }

        try (val webServer = new MockWebServer(9125, new ClassPathResource("okta-user.json"), HttpStatus.OK)) {
            webServer.start();
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, provision());
        }
    }

    private String provision() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val principal = RegisteredServiceTestUtils.getPrincipal(RandomUtils.randomAlphabetic(8),
            Map.of("email", List.of("example@google.com"), "firstName", List.of("CAS")));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        return oktaPrincipalProvisionerAction.execute(context).getId();
    }
}

