package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;
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
    WebMvcAutoConfiguration.class,
    CasAccountManagementWebflowAutoConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    CasCoreWebAutoConfiguration.class
}, properties = "cas.account-registration.provisioning.rest.url=http://localhost:5002")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RestfulApi")
class RestfulAccountRegistrationProvisionerTests {
    @Autowired
    @Qualifier(AccountRegistrationProvisioner.BEAN_NAME)
    private AccountRegistrationProvisioner accountMgmtRegistrationProvisioner;

    @BeforeEach
    public void setup() throws Exception {
        MockRequestContext.create();
    }
    
    @Test
    void verifyOperation() throws Throwable {
        try (val webServer = new MockWebServer(5002, HttpStatus.OK)) {
            webServer.start();
            val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
            val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
            assertTrue(results.isSuccess());
        }
    }

    @Test
    void verifyOperationFails() throws Throwable {
        try (val webServer = new MockWebServer(5002, HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val registrationRequest = new AccountRegistrationRequest(Map.of("username", "casuser"));
            val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
            assertFalse(results.isSuccess());
        }
    }
}
