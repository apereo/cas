package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingDelegatedClientUserProfileProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Delegation")
public class ChainingDelegatedClientUserProfileProvisionerTests {
    @Test
    public void verifyOperation() {
        val commonProfile = new CommonProfile();
        commonProfile.setClientName("CasClient");
        commonProfile.setId("testuser");
        val client = new CasClient(new CasConfiguration("http://cas.example.org"));

        val chain = new ChainingDelegatedClientUserProfileProvisioner(List.of(mock(DelegatedClientUserProfileProvisioner.class)));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                chain.execute(CoreAuthenticationTestUtils.getPrincipal(), commonProfile, client);
            }
        });
    }
}
