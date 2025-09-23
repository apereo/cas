package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasScimAutoConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
        CasAccountManagementWebflowAutoConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class,
        CasScimAutoConfiguration.class
    },
    properties = {
        "cas.scim.target=http://localhost:9666/scim/v2",
        "cas.scim.enabled=false",
        "cas.scim.username=scim-user",
        "cas.scim.password=changeit",
        "cas.account-registration.provisioning.scim.enabled=true"
    })
@Tag("SCIM")
@EnabledIfListeningOnPort(port = 9666)
class ScimAccountRegistrationProvisionerTests {
    @Autowired
    @Qualifier(AccountRegistrationProvisioner.BEAN_NAME)
    private AccountRegistrationProvisioner accountMgmtRegistrationProvisioner;

    @Test
    void verifyOperation() throws Throwable {
        val registrationRequest = new AccountRegistrationRequest(Map.of("username", UUID.randomUUID().toString()));
        val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
        assertTrue(results.isSuccess());
    }
}
