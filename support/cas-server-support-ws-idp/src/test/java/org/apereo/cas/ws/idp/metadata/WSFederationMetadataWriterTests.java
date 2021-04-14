package org.apereo.cas.ws.idp.metadata;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationMetadataWriterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
public class WSFederationMetadataWriterTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
        val results = WSFederationMetadataWriter.produceMetadataDocument(casProperties);
        assertNotNull(results);
    }
}
