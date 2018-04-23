package org.apereo.cas.support.pac4j.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jCasClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jOAuth20ClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jSamlClientProperties;
import org.junit.Test;
import org.pac4j.cas.config.CasProtocol;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link DelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@Slf4j
public class DelegatedClientFactoryTests {

    @Test
    public void verifyFactoryForIdentifiableClients() {
        final var props = new Pac4jDelegatedAuthenticationProperties();
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

        final var factory = new DelegatedClientFactory(props);
        final Set clients = factory.build();
        assertEquals(13, clients.size());
    }

    @Test
    public void verifyFactoryForCasClients() {
        final var props = new Pac4jDelegatedAuthenticationProperties();
        final var cas = new Pac4jCasClientProperties();
        cas.setLoginUrl("https://cas.example.org/login");
        cas.setProtocol(CasProtocol.SAML.name());
        props.getCas().add(cas);

        final var factory = new DelegatedClientFactory(props);
        final Set clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForSamlClients() {
        final var props = new Pac4jDelegatedAuthenticationProperties();
        final var saml = new Pac4jSamlClientProperties();
        saml.setKeystorePath(FileUtils.getTempDirectoryPath());
        saml.setIdentityProviderMetadataPath(FileUtils.getTempDirectoryPath());
        saml.setServiceProviderMetadataPath(FileUtils.getTempDirectoryPath());
        saml.setServiceProviderEntityId("test-entityid");
        props.getSaml().add(saml);

        final var factory = new DelegatedClientFactory(props);
        final Set clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForOAuthClients() {
        final var props = new Pac4jDelegatedAuthenticationProperties();
        final var oauth = new Pac4jOAuth20ClientProperties();
        configureIdentifiableClient(oauth);
        props.getOauth2().add(oauth);

        final var factory = new DelegatedClientFactory(props);
        final Set clients = factory.build();
        assertEquals(1, clients.size());
    }

    @Test
    public void verifyFactoryForOidcClients() {
        final var props = new Pac4jDelegatedAuthenticationProperties();
        final var oidc1 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc1);
        props.getOidc().add(oidc1);

        final var oidc2 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc2);
        oidc2.setType("GOOGLE");
        props.getOidc().add(oidc2);

        final var oidc3 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc3);
        oidc3.setType("AZURE");
        oidc3.setLogoutUrl("https://example.logout");
        props.getOidc().add(oidc3);

        final var oidc4 = new Pac4jOidcClientProperties();
        configureIdentifiableClient(oidc4);
        oidc4.setType("KEYCLOAK");
        props.getOidc().add(oidc4);

        final var factory = new DelegatedClientFactory(props);
        final Set clients = factory.build();
        assertEquals(4, clients.size());
    }

    private void configureIdentifiableClient(final Pac4jIdentifiableClientProperties props) {
        props.setId("TestId");
        props.setSecret("TestSecret");
    }
}
