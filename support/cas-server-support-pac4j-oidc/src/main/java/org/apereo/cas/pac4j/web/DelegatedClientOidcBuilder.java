package org.apereo.cas.pac4j.web;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.BasePac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.oauth.client.BitbucketClient;
import org.pac4j.oauth.client.DropBoxClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.FoursquareClient;
import org.pac4j.oauth.client.GenericOAuth20Client;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.HiOrgServerClient;
import org.pac4j.oauth.client.LinkedIn2Client;
import org.pac4j.oauth.client.PayPalClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.WindowsLiveClient;
import org.pac4j.oauth.client.WordPressClient;
import org.pac4j.oauth.client.YahooClient;
import org.pac4j.oidc.client.AppleClient;
import org.pac4j.oidc.client.AzureAd2Client;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.client.KeycloakOidcClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.AppleOidcConfiguration;
import org.pac4j.oidc.config.AzureAd2OidcConfiguration;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import org.pac4j.oidc.config.OidcConfiguration;

/**
 * This is {@link DelegatedClientOidcBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientOidcBuilder implements ConfigurableDelegatedClientBuilder {

    private final CasSSLContext casSslContext;

    @Override
    public List<ConfigurableDelegatedClient> build(final CasConfigurationProperties casProperties) {
        val newClients = new ArrayList<ConfigurableDelegatedClient>();
        newClients.addAll(buildFacebookIdentityProviders(casProperties));
        newClients.addAll(buildOidcIdentityProviders(casProperties));
        newClients.addAll(buildOAuth20IdentityProviders(casProperties));
        newClients.addAll(buildTwitterIdentityProviders(casProperties));
        newClients.addAll(buildDropBoxIdentityProviders(casProperties));
        newClients.addAll(buildFoursquareIdentityProviders(casProperties));
        newClients.addAll(buildGitHubIdentityProviders(casProperties));
        newClients.addAll(buildGoogleIdentityProviders(casProperties));
        newClients.addAll(buildWindowsLiveIdentityProviders(casProperties));
        newClients.addAll(buildYahooIdentityProviders(casProperties));
        newClients.addAll(buildLinkedInIdentityProviders(casProperties));
        newClients.addAll(buildPaypalIdentityProviders(casProperties));
        newClients.addAll(buildWordpressIdentityProviders(casProperties));
        newClients.addAll(buildBitBucketIdentityProviders(casProperties));
        newClients.addAll(buildHiOrgServerIdentityProviders(casProperties));
        return newClients;
    }

    protected Collection<ConfigurableDelegatedClient> buildFoursquareIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val foursquare = pac4jProperties.getFoursquare();
        if (foursquare.isEnabled() && StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
            val client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, foursquare));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildGoogleIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val google = pac4jProperties.getGoogle();
        if (google.isEnabled() && StringUtils.isNotBlank(google.getId()) && StringUtils.isNotBlank(google.getSecret())) {
            val client = new Google2Client(google.getId(), google.getSecret());
            if (StringUtils.isNotBlank(google.getScope())) {
                client.setScope(Google2Client.Google2Scope.valueOf(google.getScope().toUpperCase(Locale.ENGLISH)));
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, google));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildFacebookIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val fb = pac4jProperties.getFacebook();
        if (fb.isEnabled() && StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
            val client = new FacebookClient(fb.getId(), fb.getSecret());
            FunctionUtils.doIfNotBlank(fb.getScope(), _ -> client.setScope(fb.getScope()));
            FunctionUtils.doIfNotBlank(fb.getFields(), _ -> client.setFields(fb.getFields()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, fb));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildLinkedInIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val ln = pac4jProperties.getLinkedIn();
        if (ln.isEnabled() && StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            val client = new LinkedIn2Client(ln.getId(), ln.getSecret());
            FunctionUtils.doIfNotBlank(ln.getScope(), _ -> client.setScope(ln.getScope()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, ln));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildGitHubIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val github = pac4jProperties.getGithub();
        if (github.isEnabled() && StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            val client = new GitHubClient(github.getId(), github.getSecret());
            FunctionUtils.doIfNotBlank(github.getScope(), _ -> client.setScope(github.getScope()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, github));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildDropBoxIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val db = pac4jProperties.getDropbox();
        if (db.isEnabled() && StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            val client = new DropBoxClient(db.getId(), db.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, db));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildWindowsLiveIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val live = pac4jProperties.getWindowsLive();
        if (live.isEnabled() && StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
            val client = new WindowsLiveClient(live.getId(), live.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, live));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildYahooIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val yahoo = pac4jProperties.getYahoo();
        if (yahoo.isEnabled() && StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
            val client = new YahooClient(yahoo.getId(), yahoo.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, yahoo));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildHiOrgServerIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val hiOrgServer = pac4jProperties.getHiOrgServer();
        if (hiOrgServer.isEnabled() && StringUtils.isNotBlank(hiOrgServer.getId()) && StringUtils.isNotBlank(hiOrgServer.getSecret())) {
            val client = new HiOrgServerClient(hiOrgServer.getId(), hiOrgServer.getSecret());

            if (StringUtils.isNotBlank(hiOrgServer.getScope())) {
                client.getConfiguration().setScope(hiOrgServer.getScope());
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, hiOrgServer));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildOAuth20IdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getOauth2()
            .stream()
            .filter(oauth -> oauth.isEnabled()
                && StringUtils.isNotBlank(oauth.getId())
                && StringUtils.isNotBlank(oauth.getSecret()))
            .map(oauth -> {
                val client = new GenericOAuth20Client();
                client.setProfileId(StringUtils.defaultIfBlank(oauth.getPrincipalIdAttribute(), pac4jProperties.getCore().getPrincipalIdAttribute()));
                client.setKey(SpringExpressionLanguageValueResolver.getInstance().resolve(oauth.getId()));
                client.setSecret(SpringExpressionLanguageValueResolver.getInstance().resolve(oauth.getSecret()));
                client.setProfileAttrs(oauth.getProfileAttrs());
                client.setProfileUrl(oauth.getProfileUrl());
                client.setProfileVerb(Verb.valueOf(oauth.getProfileVerb().toUpperCase(Locale.ENGLISH)));
                client.setTokenUrl(oauth.getTokenUrl());
                client.setAuthUrl(oauth.getAuthUrl());
                client.setScope(oauth.getScope());
                client.setCustomParams(oauth.getCustomParams());
                client.setWithState(oauth.isWithState());
                FunctionUtils.doIfNotBlank(oauth.getClientAuthenticationMethod(), client::setClientAuthenticationMethod);
                client.getConfiguration().setResponseType(oauth.getResponseType());

                LOGGER.debug("Created client [{}]", client);
                return new ConfigurableDelegatedClient(client, oauth);
            })
            .collect(Collectors.toList());
    }

    protected Collection<ConfigurableDelegatedClient> buildOidcIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getOidc()
            .stream()
            .map(this::getOidcClientFrom)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected Collection<ConfigurableDelegatedClient> buildWordpressIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val wp = pac4jProperties.getWordpress();
        if (wp.isEnabled() && StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
            val client = new WordPressClient(wp.getId(), wp.getSecret());

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, wp));
        }
        return List.of();
    }

    protected Collection<ConfigurableDelegatedClient> buildTwitterIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val twitter = pac4jProperties.getTwitter();
        if (twitter.isEnabled() && StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
            val client = new TwitterClient(twitter.getId(), twitter.getSecret(), twitter.isIncludeEmail());

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, twitter));
        }
        return List.of();
    }

    private ConfigurableDelegatedClient getOidcClientFrom(final Pac4jOidcClientProperties clientProperties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        if (clientProperties.getAzure().isEnabled() && StringUtils.isNotBlank(clientProperties.getAzure().getId())) {
            LOGGER.debug("Building OpenID Connect client for Azure AD...");
            val azure = getOidcConfigurationForClient(clientProperties.getAzure(), AzureAd2OidcConfiguration.class);
            azure.setTenant(resolver.resolve(clientProperties.getAzure().getTenant()));
            val cfg = new AzureAd2OidcConfiguration(azure);
            return new ConfigurableDelegatedClient(new AzureAd2Client(cfg), clientProperties.getAzure());
        }
        if (clientProperties.getGoogle().isEnabled() && StringUtils.isNotBlank(clientProperties.getGoogle().getId())) {
            LOGGER.debug("Building OpenID Connect client for Google...");
            val cfg = getOidcConfigurationForClient(clientProperties.getGoogle(), OidcConfiguration.class);
            return new ConfigurableDelegatedClient(new GoogleOidcClient(cfg), clientProperties.getGoogle());
        }
        if (clientProperties.getKeycloak().isEnabled() && StringUtils.isNotBlank(clientProperties.getKeycloak().getId())) {
            LOGGER.debug("Building OpenID Connect client for KeyCloak...");
            val cfg = getOidcConfigurationForClient(clientProperties.getKeycloak(), KeycloakOidcConfiguration.class);
            cfg.setRealm(resolver.resolve(clientProperties.getKeycloak().getRealm()));
            cfg.setBaseUri(resolver.resolve(clientProperties.getKeycloak().getBaseUri()));
            return new ConfigurableDelegatedClient(new KeycloakOidcClient(cfg), clientProperties.getKeycloak());
        }
        if (clientProperties.getApple().isEnabled() && StringUtils.isNotBlank(clientProperties.getApple().getPrivateKey())) {
            LOGGER.debug("Building OpenID Connect client for Apple...");
            val cfg = getOidcConfigurationForClient(clientProperties.getApple(), AppleOidcConfiguration.class);

            FunctionUtils.doUnchecked(_ -> {
                val factory = new PrivateKeyFactoryBean();
                factory.setAlgorithm("EC");
                factory.setSingleton(false);
                factory.setLocation(ResourceUtils.getResourceFrom(clientProperties.getApple().getPrivateKey()));
                cfg.setPrivateKey((ECPrivateKey) factory.getObject());
            });

            cfg.setPrivateKeyID(clientProperties.getApple().getPrivateKeyId());
            cfg.setTeamID(clientProperties.getApple().getTeamId());
            cfg.setTimeout(Beans.newDuration(clientProperties.getApple().getTimeout()));
            return new ConfigurableDelegatedClient(new AppleClient(cfg), clientProperties.getApple());
        }

        if (clientProperties.getGeneric().isEnabled()) {
            LOGGER.debug("Building generic OpenID Connect client...");
            val generic = getOidcConfigurationForClient(clientProperties.getGeneric(), OidcConfiguration.class);
            return new ConfigurableDelegatedClient(new OidcClient(generic), clientProperties.getGeneric());
        }
        return null;
    }

    protected Collection<ConfigurableDelegatedClient> buildPaypalIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val paypal = pac4jProperties.getPaypal();
        if (paypal.isEnabled() && StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            val client = new PayPalClient(paypal.getId(), paypal.getSecret());

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, paypal));
        }
        return List.of();
    }

    private <T extends OidcConfiguration> T getOidcConfigurationForClient(final BasePac4jOidcClientProperties oidc,
                                                                          final Class<T> clazz) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        val cfg = FunctionUtils.doUnchecked(() -> clazz.getDeclaredConstructor().newInstance());
        FunctionUtils.doIfNotBlank(oidc.getScope(), _ -> cfg.setScope(resolver.resolve(oidc.getScope())));

        cfg.setUseNonce(oidc.isUseNonce());
        cfg.setDisablePkce(oidc.isDisablePkce());

        cfg.setSecret(resolver.resolve(oidc.getSecret()));
        cfg.setClientId(resolver.resolve(oidc.getId()));

        cfg.setReadTimeout((int) Beans.newDuration(oidc.getReadTimeout()).toMillis());
        cfg.setConnectTimeout((int) Beans.newDuration(oidc.getConnectTimeout()).toMillis());
        if (StringUtils.isNotBlank(oidc.getPreferredJwsAlgorithm())) {
            cfg.setPreferredJwsAlgorithm(JWSAlgorithm.parse(oidc.getPreferredJwsAlgorithm().toUpperCase(Locale.ENGLISH)));
        }
        cfg.setMaxClockSkew(Long.valueOf(Beans.newDuration(oidc.getMaxClockSkew()).toSeconds()).intValue());
        cfg.setDiscoveryURI(oidc.getDiscoveryUri());
        cfg.setCustomParams(oidc.getCustomParams());
        cfg.setLogoutUrl(oidc.getLogoutUrl());
        cfg.setAllowUnsignedIdTokens(oidc.isAllowUnsignedIdTokens());
        cfg.setIncludeAccessTokenClaimsInProfile(oidc.isIncludeAccessTokenClaims());
        cfg.setExpireSessionWithToken(oidc.isExpireSessionWithToken());
        cfg.setLogoutValidation(oidc.isValidateLogoutToken());
        
        FunctionUtils.doIfNotBlank(oidc.getSupportedClientAuthenticationMethods(), methods -> {
            val clientMethods = org.springframework.util.StringUtils.commaDelimitedListToSet(methods)
                .stream()
                .map(ClientAuthenticationMethod::parse)
                .collect(Collectors.toSet());
            cfg.setSupportedClientAuthenticationMethods(clientMethods);
        });

        FunctionUtils.doIfNotBlank(oidc.getClientAuthenticationMethod(),
            method -> cfg.setClientAuthenticationMethod(ClientAuthenticationMethod.parse(method)));

        if (StringUtils.isNotBlank(oidc.getTokenExpirationAdvance())) {
            cfg.setTokenExpirationAdvance((int) Beans.newDuration(oidc.getTokenExpirationAdvance()).toSeconds());
        }

        FunctionUtils.doIfNotBlank(oidc.getResponseMode(), _ -> cfg.setResponseMode(oidc.getResponseMode()));
        FunctionUtils.doIfNotBlank(oidc.getResponseType(), _ -> cfg.setResponseType(oidc.getResponseType()));

        if (!oidc.getMappedClaims().isEmpty()) {
            cfg.setMappedClaims(CollectionUtils.convertDirectedListToMap(oidc.getMappedClaims()));
        }
        cfg.setSslSocketFactory(casSslContext.getSslContext().getSocketFactory());
        cfg.setHostnameVerifier(casSslContext.getHostnameVerifier());
        return cfg;
    }

    protected Collection<ConfigurableDelegatedClient> buildBitBucketIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val bitbucket = pac4jProperties.getBitbucket();
        if (bitbucket.isEnabled() && StringUtils.isNotBlank(bitbucket.getId()) && StringUtils.isNotBlank(bitbucket.getSecret())) {
            val client = new BitbucketClient(bitbucket.getId(), bitbucket.getSecret());

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(new ConfigurableDelegatedClient(client, bitbucket));
        }
        return List.of();
    }

}
