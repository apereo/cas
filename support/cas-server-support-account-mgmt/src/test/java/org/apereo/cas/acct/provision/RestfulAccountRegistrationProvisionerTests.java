package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulAccountRegistrationProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasAccountManagementWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    CasCoreHttpConfiguration.class
}, properties = "cas.account-registration.provisioning.rest.url=http://localhost:5002")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RestfulApi")
public class RestfulAccountRegistrationProvisionerTests {
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
        try (val webServer = new MockWebServer(5002, HttpStatus.OK)) {
            webServer.start();
            val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
            val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
            assertTrue(results.isSuccess());
        }
    }

    @Test
    public void verifyOperationFails() throws Exception {
        try (val webServer = new MockWebServer(5002, HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
            val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
            assertFalse(results.isSuccess());
        }
    }
}
