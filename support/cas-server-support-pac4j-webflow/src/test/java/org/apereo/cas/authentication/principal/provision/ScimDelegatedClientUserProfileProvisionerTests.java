package org.apereo.cas.authentication.principal.provision;

import module java.base;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasScimAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScimDelegatedClientUserProfileProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SCIM")
@EnabledIfListeningOnPort(port = 9666)
@ImportAutoConfiguration({
    CasScimAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.enabled=false",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit",
    "cas.authn.pac4j.provisioning.scim.enabled=true"
})
class ScimDelegatedClientUserProfileProvisionerTests extends BaseCasCoreTests {
    @Autowired
    @Qualifier("pac4jScimDelegatedClientUserProfileProvisioner")
    private Supplier<DelegatedClientUserProfileProvisioner> pac4jScimDelegatedClientUserProfileProvisioner;

    @Test
    void verifyOperation() throws Throwable {
        val provisioner = pac4jScimDelegatedClientUserProfileProvisioner.get();
        assertNotNull(provisioner);
        var profile = new CommonProfile();
        profile.setId(UUID.randomUUID().toString());
        provisioner.execute(RegisteredServiceTestUtils.getPrincipal(profile.getId()),
            profile, mock(BaseClient.class),
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword(profile.getId(), "password"));
    }
}
