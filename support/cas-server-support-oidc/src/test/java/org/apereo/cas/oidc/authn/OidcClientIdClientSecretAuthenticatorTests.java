package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCAuthentication")
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=casuser",
    
    "cas.authn.oidc.core.user-defined-scopes.MyScope=uid",
    "cas.authn.oidc.discovery.scopes=openid,profile,email,MyScope",
    "cas.authn.oidc.discovery.claims=sub,name,family_name,given_name,uid"
})
class OidcClientIdClientSecretAuthenticatorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oauthClientAuthenticator")
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @Test
    void verifyWithoutRequestingScopes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), regexServiceUrl());
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue());
        request.addParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
        val credentialsProfile = authenticator.authenticate(newClientAuthenticationToken(request,
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue()));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(1, credentialsProfile.orElseThrow().getAttributes().size());
        assertTrue(credentialsProfile.orElseThrow().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyWithRequestingScopes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
        registeredService.setScopes(Set.of(OidcConstants.StandardScopes.OPENID.getScope(), "MyScope"));
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue());
        request.addParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope() + " MyScope");
        val credentialsProfile = authenticator.authenticate(newClientAuthenticationToken(request,
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue()));
        assertTrue(credentialsProfile.isPresent());
        val attributes = credentialsProfile.orElseThrow().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey(OAuth20Constants.CLIENT_ID));
        assertTrue(attributes.containsKey("uid"));
    }
}
