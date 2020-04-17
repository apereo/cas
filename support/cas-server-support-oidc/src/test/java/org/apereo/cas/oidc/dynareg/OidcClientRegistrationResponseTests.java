package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationUtils;

import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientRegistrationResponseTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcClientRegistrationResponseTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val service = getOidcRegisteredService();
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048);
        service.setJwks(new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        val input = OidcClientRegistrationUtils.getClientRegistrationResponse(service, "https://example.com/cas");
        assertNotNull(input.getGrantTypes());
        assertNotNull(input.getResponseTypes());
        assertNotNull(input.getRedirectUris());
        assertNotNull(input.getContacts());
    }
}
