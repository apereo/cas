package org.apereo.cas.syncope;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeAccountRegistrationProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 18080)
@SpringBootTest(classes = BaseSyncopeTests.SharedTestConfiguration.class, properties = {
    "cas.account-registration.provisioning.syncope.url=http://localhost:18080/syncope",
    "cas.account-registration.provisioning.syncope.basic-auth-username=admin",
    "cas.account-registration.provisioning.syncope.basic-auth-password=password"
})
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
class SyncopeAccountRegistrationProvisionerTests {
    @Autowired
    @Qualifier(AccountRegistrationProvisioner.BEAN_NAME)
    private AccountRegistrationProvisioner accountMgmtRegistrationProvisioner;

    @Test
    void verifySubmitUser() throws Throwable {
        val registrationRequest = new AccountRegistrationRequest(
            Map.of("username", UUID.randomUUID().toString(),
                "password", RandomUtils.randomAlphabetic(8),
                "email", "example@apereo.org"));
        val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
        assertTrue(results.isSuccess());
        assertTrue(results.containsProperty("Master"));
    }

    @Test
    void verifySubmitUserByUnknownRealm() throws Throwable {
        val registrationRequest = new AccountRegistrationRequest(
            Map.of("username", UUID.randomUUID().toString(),
                "realm", "unknown-realm",
                "password", RandomUtils.randomAlphabetic(8),
                "email", "example@apereo.org"));
        val results = accountMgmtRegistrationProvisioner.provision(registrationRequest);
        assertTrue(results.isFailure());
    }
}
