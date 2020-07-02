package org.apereo.cas.authentication;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SecurityTokenServiceTokenFetcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
    "cas.authn.wsfed-idp.idp.realm-name=CAS",

    "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
    "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
    "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
    "cas.authn.wsfed-idp.sts.encrypt-tokens=true",

    "cas.authn.wsfed-idp.sts.realm.keystore-file=classpath:stsrealm_a.jks",
    "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
    "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
    "cas.authn.wsfed-idp.sts.realm.key-password=realma",
    "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
})
@Tag("WSFederation")
public class SecurityTokenServiceTokenFetcherTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("securityTokenServiceTokenFetcher")
    private SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher;

    @Test
    public void verifySecurityPopulator() {
        val realm = casProperties.getAuthn().getWsfedIdp().getIdp().getRealm();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm(realm);
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo(realm);
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val service = CoreAuthenticationTestUtils.getService("http://example.org?"
            + WSFederationConstants.WREPLY + '=' + registeredService.getServiceId() + '&'
            + WSFederationConstants.WTREALM + '=' + realm);

        assertThrows(AuthenticationException.class, () -> securityTokenServiceTokenFetcher.fetch(service, "test"));
    }
}
