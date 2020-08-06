package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPLogoutResponseObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
public class SamlIdPLogoutResponseObjectBuilderTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlIdPLogoutResponseObjectBuilder")
    private SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder;

    @Test
    public void verifyOperation() {
        val response = samlIdPLogoutResponseObjectBuilder.newLogoutResponse(
            UUID.randomUUID().toString(), "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer("myissuer"),
            samlIdPLogoutResponseObjectBuilder.newStatus(StatusCode.SUCCESS, null),
            "https://google.com");
        assertNotNull(response);
    }

}
