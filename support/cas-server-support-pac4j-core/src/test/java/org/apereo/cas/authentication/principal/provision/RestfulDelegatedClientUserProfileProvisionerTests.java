package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link RestfulDelegatedClientUserProfileProvisionerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApi")
public class RestfulDelegatedClientUserProfileProvisionerTests {
    @Test
    public void verifyAllowedOperation() {
        val commonProfile = new CommonProfile();
        commonProfile.setClientName("CasClient");
        commonProfile.setId("testuser");
        val client = new CasClient(new CasConfiguration("http://cas.example.org"));

        try (val webServer = new MockWebServer(9192,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9192");
            val service = new RestfulDelegatedClientUserProfileProvisioner(props);
            service.execute(CoreAuthenticationTestUtils.getPrincipal(), commonProfile, client);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
