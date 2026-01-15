package org.apereo.cas.authentication.principal.provision;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link GroovyDelegatedClientUserProfileProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
class GroovyDelegatedClientUserProfileProvisionerTests {
    @Test
    void verifyOperation() throws Throwable {
        val provisioner = new GroovyDelegatedClientUserProfileProvisioner(new ClassPathResource("delegated-provisioner.groovy"));
        val commonProfile = new CommonProfile();
        commonProfile.setClientName("CasClient");
        commonProfile.setId("testuser");
        val client = new CasClient(new CasConfiguration("http://cas.example.org"));
        provisioner.execute(CoreAuthenticationTestUtils.getPrincipal(), commonProfile, client,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
    }
}
