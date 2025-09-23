package org.apereo.cas.web;

import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.token.authentication.TokenCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Authentication")
class TokenCredentialTests {
    @Test
    void verifyTokenFromParameter() {
        val credential = new TokenCredential("tokenid", RegisteredServiceTestUtils.getService());
        val metadata = new BasicCredentialMetadata(credential);
        assertNotNull(metadata.getCredentialClass());
    }
}
