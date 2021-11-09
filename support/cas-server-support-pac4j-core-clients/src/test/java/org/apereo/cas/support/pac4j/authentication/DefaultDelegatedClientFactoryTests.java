package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.cas.Pac4jCasClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oauth.Pac4jOAuth20ClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.nimbusds.jose.JWSAlgorithm;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Delegation")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
})
public class DefaultDelegatedClientFactoryTests {
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casSslContext")
    private CasSSLContext casSslContext;

    private static Pac4jSamlClientProperties getPac4jSamlClientProperties(final String sessionFactory) throws Exception {
        val saml = new Pac4jSamlClientProperties();
        saml.setKeystorePath(new File(FileUtils.getTempDirectoryPath(), "keystore.jks").getCanonicalPath());
        saml.setKeystoreAlias("alias1");
        saml.setCallbackUrlType(Pac4jBaseClientProperties.CallbackUrlTypes.NONE);
        saml.setKeystorePassword("1234567890");
        saml.setPrivateKeyPassword("1234567890");
        saml.setIdentityProviderMetadataPath("classpath:idp-metadata.xml");
        saml.setServiceProviderMetadataPath(new File(FileUtils.getTempDirectoryPath(), "sp.xml").getCanonicalPath());
        saml.setServiceProviderEntityId("test-entityid");
        saml.setForceKeystoreGeneration(true);
        saml.setMessageStoreFactory(sessionFactory);
        saml.setPrincipalIdAttribute("givenName");
        saml.setAssertionConsumerServiceIndex(1);
        saml.setAuthnContextClassRef(List.of("classRef1"));
        saml.setNameIdPolicyFormat("transient");
        saml.setBlockedSignatureSigningAlgorithms(List.of("sha-1"));
        saml.setSignatureAlgorithms(List.of("sha-256"));
        saml.setSignatureReferenceDigestMethods(List.of("sha-256"));
        saml.getRequestedAttributes().add(
            new Pac4jSamlClientProperties.ServiceProviderRequestedAttribute()
                .setName("requestedAttribute")
                .setFriendlyName("friendlyRequestedName"));
        saml.setMappedAttributes(List.of("attr1->givenName"));
        return saml;
    }

    private static void configureIdentifiableClient(final Pac4jIdentifiableClientProperties props) {
        props.setId("TestId");
        props.setSecret("TestSecret");
    }

    @BeforeEach
    public void setup() {
        this.applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
    }

    @Test
    public void verifyFactoryForIdentifiableClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        configureIdentifiableClient(props.getBitbucket());
        configureIdentifiableClient(props.getDropbox());

        configureIdentifiableClient(props.getFacebook());
        props.getFacebook().setFields("field1,field2");
        props.getFacebook().setScope("scope1");
        configureIdentifiableClient(props.getFoursquare());
        configureIdentifiableClient(props.getGithub());

        configureIdentifiableClient(props.getGoogle());
        props.getGoogle().setScope("EMAIL_AND_PROFILE");

        configureIdentifiableClient(props.getLinkedIn());
        props.getLinkedIn().setScope("scope1");

        configureIdentifiableClient(props.getPaypal());
        configureIdentifiableClient(props.getTwitter());
        configureIdentifiableClient(props.getWindowsLive());
        configureIdentifiableClient(props.getWordpress());
        configureIdentifiableClient(props.getYahoo());
        configureIdentifiableClient(props.getHiOrgServer());
        props.getHiOrgServer().setScope("scope1");

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(13, clients.size());
        factory.destroy();
    }

    @Test
    public void verifyFactoryForCasClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val cas = new Pac4jCasClientProperties();
        cas.setLoginUrl("https://cas.example.org/login");
        cas.setProtocol(CasProtocol.SAML.name());
        cas.setPrincipalAttributeId("uid");
        cas.setCssClass("cssclass");
        cas.setDisplayName("My CAS Server");
        props.getCore().setLazyInit(false);
        props.getCas().add(cas);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForCasClientsHavingLoginInDomain() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val cas = new Pac4jCasClientProperties();
        cas.setLoginUrl("https://login.example.org/login");
        cas.setProtocol(CasProtocol.SAML.name());
        props.getCas().add(cas);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
        val client = (CasClient) clients.iterator().next();
        assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
    }

    @Test
    public void verifyFactoryForSamlClients() throws Exception {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val saml = getPac4jSamlClientProperties(HttpSessionStoreFactory.class.getName());
        props.getSaml().add(saml);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());

        assertTrue(SAML2Client.class.cast(clients.iterator().next()).getConfiguration().
            getSamlMessageStoreFactory() instanceof HttpSessionStoreFactory);
    }

    @Test
    public void verifyBadSessionStoreForSamlClients() throws Exception {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val saml = getPac4jSamlClientProperties("BadClassName");
        props.getSaml().add(saml);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifySamlClientCustomMessageStoreFactory() throws Exception {
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            mock(SAMLMessageStoreFactory.class), DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY);

        val props = new Pac4jDelegatedAuthenticationProperties();
        val saml = getPac4jSamlClientProperties("bad.type.name.ignored.for.bean");
        props.getSaml().add(saml);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForOAuthClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val oauth = new Pac4jOAuth20ClientProperties();
        configureIdentifiableClient(oauth);
        props.getOauth2().add(oauth);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyGithubClient() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        configureIdentifiableClient(props.getGithub());
        props.getGithub().setScope("user");
        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
        val client = (GitHubClient) clients.iterator().next();
        assertEquals("user", client.getScope());
    }

    @Test
    public void verifyFactoryForAppleOidcClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();

        val oidc1 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc1.getGeneric());
        oidc1.getApple().setPrivateKey("classpath:apple.pem");
        oidc1.getApple().setPrivateKeyId("VB4MYGJ3TQ");
        oidc1.getApple().setTeamId("67D9XQG2LJ");
        oidc1.getApple().setResponseType("code id_token");
        oidc1.getApple().setResponseMode("form_post");
        oidc1.getApple().setScope("openid name email");
        oidc1.getApple().setId("org.pac4j.test");
        oidc1.getApple().setUseNonce(true);
        oidc1.getApple().setEnabled(true);
        props.getOidc().add(oidc1);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForOidcClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();

        val oidc1 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc1.getGeneric());
        oidc1.getGeneric().setResponseMode("query");
        oidc1.getGeneric().setScope("scope1");
        oidc1.getGeneric().setResponseType("none");
        oidc1.getGeneric().setTokenExpirationAdvance("PT5S");
        oidc1.getGeneric().setPreferredJwsAlgorithm(JWSAlgorithm.RS256.getName());
        oidc1.getGeneric().setDiscoveryUri("https://dev-425954.oktapreview.com/.well-known/openid-configuration");
        oidc1.getGeneric().getMappedClaims().add("claim1->attribute1");
        props.getOidc().add(oidc1);

        val oidc2 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc2.getGoogle());
        props.getOidc().add(oidc2);

        val oidc3 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc3.getAzure());
        oidc3.getAzure().setTenant("contoso.onmicrosoft.com");
        oidc3.getAzure().setLogoutUrl("https://example.logout");
        props.getOidc().add(oidc3);

        val oidc4 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc4.getKeycloak());
        oidc4.getKeycloak().setRealm("master");
        oidc4.getKeycloak().setBaseUri("https://dev-425954.oktapreview.com/");
        props.getOidc().add(oidc4);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = getDefaultDelegatedClientFactory(casSettings);
        val clients = factory.build();
        assertEquals(4, clients.size());
    }

    private DefaultDelegatedClientFactory getDefaultDelegatedClientFactory(final CasConfigurationProperties casSettings) {
        return new DefaultDelegatedClientFactory(casSettings, List.of(), casSslContext, applicationContext);
    }
}
