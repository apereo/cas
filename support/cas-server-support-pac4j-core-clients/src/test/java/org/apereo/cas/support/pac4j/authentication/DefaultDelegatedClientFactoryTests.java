package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.cas.Pac4jCasClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oauth.Pac4jOAuth20ClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
public class DefaultDelegatedClientFactoryTests {

    private static void configureIdentifiableClient(final Pac4jIdentifiableClientProperties props) {
        props.setId("TestId");
        props.setSecret("TestSecret");
    }

    @Test
    public void verifyFactoryForIdentifiableClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        configureIdentifiableClient(props.getBitbucket());
        configureIdentifiableClient(props.getDropbox());
        configureIdentifiableClient(props.getFacebook());
        configureIdentifiableClient(props.getFoursquare());
        configureIdentifiableClient(props.getGithub());
        configureIdentifiableClient(props.getGoogle());
        configureIdentifiableClient(props.getLinkedIn());
        configureIdentifiableClient(props.getOrcid());
        configureIdentifiableClient(props.getPaypal());
        configureIdentifiableClient(props.getTwitter());
        configureIdentifiableClient(props.getWindowsLive());
        configureIdentifiableClient(props.getWordpress());
        configureIdentifiableClient(props.getYahoo());
        configureIdentifiableClient(props.getHiOrgServer());

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
        val clients = factory.build();
        assertEquals(14, clients.size());
    }

    @Test
    public void verifyFactoryForCasClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val cas = new Pac4jCasClientProperties();
        cas.setLoginUrl("https://cas.example.org/login");
        cas.setProtocol(CasProtocol.SAML.name());
        props.getCas().add(cas);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
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
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
        val clients = factory.build();
        assertEquals(1, clients.size());
        val client = (CasClient) clients.iterator().next();
        assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
    }

    @Test
    public void verifyFactoryForSamlClients() throws Exception {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val saml = new Pac4jSamlClientProperties();
        saml.setKeystorePath(new File(FileUtils.getTempDirectoryPath(), "keystore.jks").getCanonicalPath());
        saml.setKeystorePassword("1234567890");
        saml.setPrivateKeyPassword("1234567890");
        saml.setIdentityProviderMetadataPath("classpath:idp-metadata.xml");
        saml.setServiceProviderMetadataPath(new File(FileUtils.getTempDirectoryPath(), "sp.xml").getCanonicalPath());
        saml.setServiceProviderEntityId("test-entityid");
        saml.setForceKeystoreGeneration(true);
        saml.setMessageStoreFactory(HttpSessionStoreFactory.class.getName());
        props.getSaml().add(saml);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
        val clients = factory.build();
        assertEquals(1, clients.size());

        assertTrue(SAML2Client.class.cast(clients.iterator().next()).getConfiguration().
            getSamlMessageStoreFactory() instanceof HttpSessionStoreFactory);
    }

    @Test
    public void verifyFactoryForOAuthClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();
        val oauth = new Pac4jOAuth20ClientProperties();
        configureIdentifiableClient(oauth);
        props.getOauth2().add(oauth);

        val casSettings = new CasConfigurationProperties();
        casSettings.getAuthn().setPac4j(props);
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
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
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
        val clients = factory.build();
        assertEquals(1, clients.size());
        val client = (GitHubClient) clients.iterator().next();
        assertEquals("user", client.getScope());
    }

    @Test
    public void verifyFactoryForOidcClients() {
        val props = new Pac4jDelegatedAuthenticationProperties();

        val oidc1 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc1.getGeneric());
        oidc1.getGeneric().setDiscoveryUri("https://dev-425954.oktapreview.com/.well-known/openid-configuration");
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
        val factory = new DefaultDelegatedClientFactory(casSettings, List.of());
        val clients = factory.build();
        assertEquals(4, clients.size());
    }
}
