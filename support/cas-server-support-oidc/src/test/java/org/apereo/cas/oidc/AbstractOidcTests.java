package org.apereo.cas.oidc;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasOAuthThrottleConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.IdTokenSigningAndEncryptionService;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.execution.Action;

import java.util.Optional;

/**
 * This is {@link AbstractOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    OidcConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCookieConfiguration.class,
    CasThemesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasOAuthConfiguration.class,
    CasThrottlingConfiguration.class,
    CasOAuthThrottleConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class
})
@DirtiesContext
@Category(FileSystemCategory.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.issuer=https://sso.example.org/cas/oidc",
    "cas.authn.oidc.jwksFile=classpath:keystore.jwks"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class AbstractOidcTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("profileScopeToAttributesFilter")
    protected OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("oidcDefaultJsonWebKeystoreCache")
    protected LoadingCache<String, Optional<RsaJsonWebKey>> oidcDefaultJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcTokenSigningAndEncryptionService")
    protected IdTokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService;

    @Autowired
    @Qualifier("oidcServiceJsonWebKeystoreCache")
    protected LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> oidcServiceJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcJsonWebKeystoreGeneratorService")
    protected OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    @Autowired
    @Qualifier("oidcRegisteredServiceUIAction")
    protected Action oidcRegisteredServiceUIAction;

    @Autowired
    @Qualifier("oidcServerDiscoverySettingsFactory")
    protected OidcServerDiscoverySettings oidcServerDiscoverySettings;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("oidcIdTokenGenerator")
    protected IdTokenGeneratorService oidcIdTokenGenerator;

    @BeforeEach
    public void initialize() {
        servicesManager.save(getOidcRegisteredService());
    }

    protected OidcRegisteredService getOidcRegisteredService() {
        return getOidcRegisteredService(true, true);
    }

    protected OidcRegisteredService getOidcRegisteredService(final boolean sign, final boolean encrypt) {
        val svc = new OidcRegisteredService();
        svc.setClientId("clientid");
        svc.setName("oauth");
        svc.setDescription("description");
        svc.setClientSecret("secret");
        svc.setServiceId("https://oauth\\.example\\.org.*");
        svc.setSignIdToken(sign);
        svc.setEncryptIdToken(encrypt);
        svc.setIdTokenEncryptionAlg(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        svc.setIdTokenEncryptionEncoding(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        svc.setInformationUrl("info");
        svc.setPrivacyUrl("privacy");
        svc.setJwks("classpath:keystore.jwks");
        return svc;
    }

    protected JwtClaims getClaims() {
        val claims = new JwtClaims();
        claims.setJwtId(RandomStringUtils.randomAlphanumeric(16));
        claims.setIssuer("https://cas.example.org");
        claims.setAudience(getOidcRegisteredService().getClientId());

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(120);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(1);
        claims.setSubject("casuser");
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, "clientid");
        return claims;
    }
}
