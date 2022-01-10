package org.apereo.cas.support.wsfederation.authentication.crypto;

import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationMetadataCertificateProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("WSFederation")
@TestPropertySource(properties = {
    "cas.authn.wsfed[0].signing-certificate-resources=classpath:FederationMetadata.xml,classpath:adfs-signing.crt",
    "cas.authn.wsfed[0].identity-provider-identifier=http://adfs.example.com/adfs/services/trust"
})
public class WsFederationMetadataCertificateProviderTests extends AbstractWsFederationTests {

    @Test
    public void verifyOperation() throws Exception {
        assertFalse(wsFederationConfigurations.toList().isEmpty());
        val provider = WsFederationCertificateProvider.getProvider(wsFederationConfigurations.first(), configBean);
        assertEquals(2, provider.getSigningCredentials().size());
    }
}
