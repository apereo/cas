package org.apereo.cas.support.pac4j.authentication;

import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jOidcClientProperties;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.BaseClient;
import org.pac4j.oauth.client.BitbucketClient;
import org.pac4j.oauth.client.DropBoxClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.FoursquareClient;
import org.pac4j.oauth.client.GenericOAuth20Client;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.LinkedIn2Client;
import org.pac4j.oauth.client.OrcidClient;
import org.pac4j.oauth.client.PayPalClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.WindowsLiveClient;
import org.pac4j.oauth.client.WordPressClient;
import org.pac4j.oauth.client.YahooClient;
import org.pac4j.oidc.client.AzureAdClient;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.client.KeycloakOidcClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.AzureAdOidcConfiguration;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link DelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientFactory {
    /**
     * The Pac 4 j properties.
     */
    private final Pac4jDelegatedAuthenticationProperties pac4jProperties;

    /**
     * Configure github client.
     *
     * @param properties the properties
     */
    protected void configureGithubClient(final Collection<BaseClient> properties) {
        final var github = pac4jProperties.getGithub();
        if (StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            final var client = new GitHubClient(github.getId(), github.getSecret());
            configureClient(client, github);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure dropbox client.
     *
     * @param properties the properties
     */
    protected void configureDropboxClient(final Collection<BaseClient> properties) {
        final var db = pac4jProperties.getDropbox();
        if (StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            final var client = new DropBoxClient(db.getId(), db.getSecret());
            configureClient(client, db);
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure orcid client.
     *
     * @param properties the properties
     */
    protected void configureOrcidClient(final Collection<BaseClient> properties) {
        final var db = pac4jProperties.getOrcid();
        if (StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            final var client = new OrcidClient(db.getId(), db.getSecret());
            configureClient(client, db);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure windows live client.
     *
     * @param properties the properties
     */
    protected void configureWindowsLiveClient(final Collection<BaseClient> properties) {
        final var live = pac4jProperties.getWindowsLive();
        if (StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
            final var client = new WindowsLiveClient(live.getId(), live.getSecret());
            configureClient(client, live);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure yahoo client.
     *
     * @param properties the properties
     */
    protected void configureYahooClient(final Collection<BaseClient> properties) {
        final var yahoo = pac4jProperties.getYahoo();
        if (StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
            final var client = new YahooClient(yahoo.getId(), yahoo.getSecret());
            configureClient(client, yahoo);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure foursquare client.
     *
     * @param properties the properties
     */
    protected void configureFoursquareClient(final Collection<BaseClient> properties) {
        final var foursquare = pac4jProperties.getFoursquare();
        if (StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
            final var client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
            configureClient(client, foursquare);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure google client.
     *
     * @param properties the properties
     */
    protected void configureGoogleClient(final Collection<BaseClient> properties) {
        final var google = pac4jProperties.getGoogle();
        final var client = new Google2Client(google.getId(), google.getSecret());
        if (StringUtils.isNotBlank(google.getId()) && StringUtils.isNotBlank(google.getSecret())) {
            configureClient(client, google);
            if (StringUtils.isNotBlank(google.getScope())) {
                client.setScope(Google2Client.Google2Scope.valueOf(google.getScope().toUpperCase()));
            }

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure facebook client.
     *
     * @param properties the properties
     */
    protected void configureFacebookClient(final Collection<BaseClient> properties) {
        final var fb = pac4jProperties.getFacebook();
        if (StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
            final var client = new FacebookClient(fb.getId(), fb.getSecret());

            configureClient(client, fb);
            if (StringUtils.isNotBlank(fb.getScope())) {
                client.setScope(fb.getScope());
            }

            if (StringUtils.isNotBlank(fb.getFields())) {
                client.setFields(fb.getFields());
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure linked in client.
     *
     * @param properties the properties
     */
    protected void configureLinkedInClient(final Collection<BaseClient> properties) {
        final var ln = pac4jProperties.getLinkedIn();
        if (StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            final var client = new LinkedIn2Client(ln.getId(), ln.getSecret());
            configureClient(client, ln);

            if (StringUtils.isNotBlank(ln.getScope())) {
                client.setScope(ln.getScope());
            }

            if (StringUtils.isNotBlank(ln.getFields())) {
                client.setFields(ln.getFields());
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure twitter client.
     *
     * @param properties the properties
     */
    protected void configureTwitterClient(final Collection<BaseClient> properties) {
        final var twitter = pac4jProperties.getTwitter();
        if (StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
            final var client = new TwitterClient(twitter.getId(), twitter.getSecret(), twitter.isIncludeEmail());
            configureClient(client, twitter);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure wordpress client.
     *
     * @param properties the properties
     */
    protected void configureWordpressClient(final Collection<BaseClient> properties) {
        final var wp = pac4jProperties.getWordpress();
        if (StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
            final var client = new WordPressClient(wp.getId(), wp.getSecret());
            configureClient(client, wp);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure bitbucket client.
     *
     * @param properties the properties
     */
    protected void configureBitbucketClient(final Collection<BaseClient> properties) {
        final var bb = pac4jProperties.getBitbucket();
        if (StringUtils.isNotBlank(bb.getId()) && StringUtils.isNotBlank(bb.getSecret())) {
            final var client = new BitbucketClient(bb.getId(), bb.getSecret());
            configureClient(client, bb);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure paypal client.
     *
     * @param properties the properties
     */
    protected void configurePaypalClient(final Collection<BaseClient> properties) {
        final var paypal = pac4jProperties.getPaypal();
        if (StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            final var client = new PayPalClient(paypal.getId(), paypal.getSecret());
            configureClient(client, paypal);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Sets client name.
     *
     * @param client the client
     * @param props  the props
     */
    protected void configureClient(final BaseClient client, final Pac4jBaseClientProperties props) {
        if (StringUtils.isNotBlank(props.getClientName())) {
            client.setName(props.getClientName());
        }
        client.getCustomProperties().put("autoRedirect", props.isAutoRedirect());
    }

    /**
     * Configure cas client.
     *
     * @param properties the properties
     */
    protected void configureCasClient(final Collection<BaseClient> properties) {
        final var index = new AtomicInteger();
        pac4jProperties.getCas()
            .stream()
            .filter(cas -> StringUtils.isNotBlank(cas.getLoginUrl()))
            .forEach(cas -> {
                final var cfg = new CasConfiguration(cas.getLoginUrl(), CasProtocol.valueOf(cas.getProtocol()));
                final var client = new CasClient(cfg);

                final var count = index.intValue();
                if (StringUtils.isBlank(cas.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                configureClient(client, cas);

                index.incrementAndGet();
                LOGGER.debug("Created client [{}]", client);
                properties.add(client);
            });
    }

    /**
     * Configure saml client.
     *
     * @param properties the properties
     */
    protected void configureSamlClient(final Collection<BaseClient> properties) {
        final var index = new AtomicInteger();
        pac4jProperties.getSaml()
            .stream()
            .filter(saml -> StringUtils.isNotBlank(saml.getKeystorePath())
                && StringUtils.isNotBlank(saml.getIdentityProviderMetadataPath())
                && StringUtils.isNotBlank(saml.getServiceProviderEntityId())
                && StringUtils.isNotBlank(saml.getServiceProviderMetadataPath()))
            .forEach(saml -> {
                final var cfg = new SAML2ClientConfiguration(saml.getKeystorePath(),
                    saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), saml.getIdentityProviderMetadataPath());
                cfg.setMaximumAuthenticationLifetime(saml.getMaximumAuthenticationLifetime());
                cfg.setServiceProviderEntityId(saml.getServiceProviderEntityId());
                cfg.setServiceProviderMetadataPath(saml.getServiceProviderMetadataPath());
                cfg.setDestinationBindingType(saml.getDestinationBinding());
                cfg.setForceAuth(saml.isForceAuth());
                cfg.setPassive(saml.isPassive());
                cfg.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                cfg.setAttributeConsumingServiceIndex(saml.getAttributeConsumingServiceIndex());
                if (saml.getAssertionConsumerServiceIndex() >= 0) {
                    cfg.setAssertionConsumerServiceIndex(saml.getAssertionConsumerServiceIndex());
                }

                if (StringUtils.isNotBlank(saml.getAuthnContextClassRef())) {
                    cfg.setComparisonType(saml.getAuthnContextComparisonType().toUpperCase());
                    cfg.setAuthnContextClassRef(saml.getAuthnContextClassRef());
                }
                if (StringUtils.isNotBlank(saml.getKeystoreAlias())) {
                    cfg.setKeystoreAlias(saml.getKeystoreAlias());
                }
                if (StringUtils.isNotBlank(saml.getNameIdPolicyFormat())) {
                    cfg.setNameIdPolicyFormat(saml.getNameIdPolicyFormat());
                }
                final var client = new SAML2Client(cfg);

                final var count = index.intValue();
                if (StringUtils.isBlank(saml.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                configureClient(client, saml);

                index.incrementAndGet();
                LOGGER.debug("Created delegated client [{}]", client);
                properties.add(client);
            });
    }

    /**
     * Configure o auth 20 client.
     *
     * @param properties the properties
     */
    protected void configureOAuth20Client(final Collection<BaseClient> properties) {
        final var index = new AtomicInteger();
        pac4jProperties.getOauth2()
            .stream()
            .filter(oauth -> StringUtils.isNotBlank(oauth.getId()) && StringUtils.isNotBlank(oauth.getSecret()))
            .forEach(oauth -> {
                final var client = new GenericOAuth20Client();
                client.setKey(oauth.getId());
                client.setSecret(oauth.getSecret());
                client.setProfileAttrs(oauth.getProfileAttrs());
                client.setProfileNodePath(oauth.getProfilePath());
                client.setProfileUrl(oauth.getProfileUrl());
                client.setProfileVerb(Verb.valueOf(oauth.getProfileVerb().toUpperCase()));
                client.setTokenUrl(oauth.getTokenUrl());
                client.setAuthUrl(oauth.getAuthUrl());
                client.setCustomParams(oauth.getCustomParams());
                final var count = index.intValue();
                if (StringUtils.isBlank(oauth.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                configureClient(client, oauth);

                index.incrementAndGet();
                LOGGER.debug("Created client [{}]", client);
                properties.add(client);
            });
    }

    /**
     * Configure oidc client.
     *
     * @param properties the properties
     */
    protected void configureOidcClient(final Collection<BaseClient> properties) {
        final var index = new AtomicInteger();
        pac4jProperties.getOidc()
            .stream()
            .filter(oidc -> StringUtils.isNotBlank(oidc.getId()) && StringUtils.isNotBlank(oidc.getSecret()))
            .forEach(oidc -> {
                final OidcClient client;
                switch (oidc.getType().toUpperCase()) {
                    case "GOOGLE":
                        final var cfg = getOidcConfigurationForClient(oidc, OidcConfiguration.class);
                        client = new GoogleOidcClient(cfg);
                        break;
                    case "AZURE":
                        final var azure = getOidcConfigurationForClient(oidc, AzureAdOidcConfiguration.class);
                        client = new AzureAdClient(new AzureAdOidcConfiguration(azure));
                        break;
                    case "KEYCLOAK":
                        final var keycfg = getOidcConfigurationForClient(oidc, KeycloakOidcConfiguration.class);
                        client = new KeycloakOidcClient(keycfg);
                        break;
                    case "GENERIC":
                    default:
                        final var gencfg = getOidcConfigurationForClient(oidc, OidcConfiguration.class);
                        client = new OidcClient(gencfg);
                        break;
                }

                final var count = index.intValue();
                if (StringUtils.isBlank(oidc.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                configureClient(client, oidc);
                index.incrementAndGet();
                LOGGER.debug("Created client [{}]", client);
                properties.add(client);
            });
    }

    @SneakyThrows
    private <T extends OidcConfiguration> T getOidcConfigurationForClient(final Pac4jOidcClientProperties oidc, final Class<T> clazz) {
        final var cfg = clazz.getDeclaredConstructor().newInstance();
        if (StringUtils.isNotBlank(oidc.getScope())) {
            cfg.setScope(oidc.getScope());
        }
        cfg.setUseNonce(oidc.isUseNonce());
        cfg.setSecret(oidc.getSecret());
        cfg.setClientId(oidc.getId());

        if (StringUtils.isNotBlank(oidc.getPreferredJwsAlgorithm())) {
            cfg.setPreferredJwsAlgorithm(JWSAlgorithm.parse(oidc.getPreferredJwsAlgorithm().toUpperCase()));
        }
        cfg.setMaxClockSkew(oidc.getMaxClockSkew());
        cfg.setDiscoveryURI(oidc.getDiscoveryUri());
        cfg.setCustomParams(oidc.getCustomParams());
        cfg.setLogoutUrl(oidc.getLogoutUrl());
        return cfg;
    }

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    public Set<BaseClient> build() {
        final Set<BaseClient> clients = new LinkedHashSet<>();

        configureCasClient(clients);
        configureFacebookClient(clients);
        configureOidcClient(clients);
        configureOAuth20Client(clients);
        configureSamlClient(clients);
        configureTwitterClient(clients);
        configureDropboxClient(clients);
        configureFoursquareClient(clients);
        configureGithubClient(clients);
        configureGoogleClient(clients);
        configureWindowsLiveClient(clients);
        configureYahooClient(clients);
        configureLinkedInClient(clients);
        configurePaypalClient(clients);
        configureWordpressClient(clients);
        configureBitbucketClient(clients);
        configureOrcidClient(clients);

        return clients;
    }
}
