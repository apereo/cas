package org.apereo.cas.oidc.web;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.scopes.ScopeResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link OidcTokenGeneratorTests}.
 *
 * @author Simon Bear
 * @since 6.5.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
        "cas.authn.oidc.access-token.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
        "cas.authn.oidc.access-token.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ",
        "cas.authn.oidc.access-token.crypto.enabled=true",
        "cas.authn.oidc.device-token.refresh-interval=PT1S",
        "cas.authn.oidc.discovery.scopes=openid,profile,read:something,write:something,urn:somecustom"
})
public class OidcTokenGeneratorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    protected OAuth20DeviceTokenFactory defaultDeviceTokenFactory;

    @Autowired
    @Qualifier("defaultDeviceUserCodeFactory")
    protected OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected OAuth20RefreshTokenFactory oAuthRefreshTokenFactory;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultScopeResolver")
    protected ScopeResolver defaultScopeResolver;

    protected void clearAllServices() {
        servicesManager.deleteAll();
        servicesManager.load();
    }

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyScopes() throws Exception {
        val registeredService = getOidcRegisteredService();
        registeredService.setScopes(Set.of("openid", "profile", "test", "read:something", "write:something"));
        servicesManager.save(registeredService);
        val generator = new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory, defaultDeviceTokenFactory,
                defaultDeviceUserCodeFactory, oAuthRefreshTokenFactory, centralAuthenticationService,
                defaultScopeResolver, casProperties);
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());

        Thread.sleep(2000);
        val holder = AccessTokenRequestContext.builder()
                .service(service)
                .responseType(OAuth20ResponseTypes.IDTOKEN_TOKEN)
                .scopes(Set.of("openid", "profile", "read:something", "test", "urn:somecustom", "nonexistent"))
                .authentication(RegisteredServiceTestUtils.getAuthentication())
                .registeredService(registeredService)
                .build();
        val result = generator.generate(holder);
        assertTrue(result.getAccessToken().isPresent());
        assertEquals(Set.of("openid", "profile", "read:something"), result.getAccessToken().get().getScopes());
    }

}
