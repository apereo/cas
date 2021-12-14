package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasScimConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimAccountRegistrationProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(
    classes = {
        RefreshAutoConfiguration.class,
        CasAccountManagementWebflowConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasScimConfiguration.class
    },
    properties = {
        "cas.scim.target=http://localhost:9666/scim/v2",
        "cas.scim.enabled=false",
        "cas.scim.version=2",
        "cas.scim.username=scim-user",
        "cas.scim.password=changeit",
        "cas.account-registration.provisioning.scim.enabled=true"
    })
@Tag("SCIM")
@EnabledIfPortOpen(port = 9666)
public class ScimAccountRegistrationProvisionerTests {
    @Autowired
    @Qualifier("accountMgmtRegistrationProvisioner")
    private AccountRegistrationProvisioner accountMgmtRegistrationProvisioner;

    @BeforeEach
    public void setup() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
    }

    @Test
    public void verifyOperation() throws Exception {
        val registrationRequest = new AccountRegistrationRequest(Map.of("username", UUID.randomUUID().toString()));
        val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
        assertTrue(results.isSuccess());
    }
}
