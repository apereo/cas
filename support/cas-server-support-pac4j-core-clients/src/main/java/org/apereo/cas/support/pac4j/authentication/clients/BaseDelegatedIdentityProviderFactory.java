package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.BasePac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.pac4j.core.profile.converter.AttributeConverter;
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
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.metadata.SAML2ServiceProviderRequestedAttribute;
import org.pac4j.saml.store.EmptyStoreFactory;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import java.security.interfaces.ECPrivateKey;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link BaseDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseDelegatedIdentityProviderFactory implements DelegatedIdentityProviderFactory {
    private static final Pattern PATTERN_LOGIN_URL = Pattern.compile('/' + CasWebflowConfigurer.FLOW_ID_LOGIN + '$');

    protected final CasConfigurationProperties casProperties;

    private final CasReentrantLock lock = new CasReentrantLock();

    private final Collection<DelegatedClientFactoryCustomizer> customizers;

    private final CasSSLContext casSSLContext;

    private final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory;

    private final Cache<String, Collection<IndirectClient>> clientsCache;

    protected abstract Collection<IndirectClient> loadIdentityProviders() throws Exception;

    @Override
    public final Collection<IndirectClient> build() {
        return lock.tryLock(() -> {
            val core = casProperties.getAuthn().getPac4j().getCore();
            val currentClients = getCachedClients().isEmpty() || !core.isLazyInit() ? loadIdentityProviders() : getCachedClients();
            clientsCache.put(casProperties.getServer().getName(), currentClients);
            return currentClients;
        });
    }

    @Override
    public Collection<IndirectClient> rebuild() {
        clientsCache.invalidateAll();
        return build();
    }

    protected Collection<IndirectClient> getCachedClients() {
        val cachedClients = clientsCache.getIfPresent(casProperties.getServer().getName());
        return ObjectUtils.defaultIfNull(cachedClients, new ArrayList<>());
    }

    protected void configureClient(final IndirectClient client,
                                   final Pac4jBaseClientProperties clientProperties,
                                   final CasConfigurationProperties givenProperties) {
        val cname = clientProperties.getClientName();
        if (StringUtils.isNotBlank(cname)) {
            client.setName(cname);
        } else {
            val className = client.getClass().getSimpleName();
            val genName = className.concat(RandomUtils.randomNumeric(4));
            client.setName(genName);
            LOGGER.warn("Client name for [{}] is set to a generated value of [{}]. "
                + "Consider defining an explicit name for the delegated provider", className, genName);
        }
        val customProperties = client.getCustomProperties();
        customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, clientProperties.getAutoRedirectType());

        FunctionUtils.doIfNotBlank(clientProperties.getPrincipalIdAttribute(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID, clientProperties.getPrincipalIdAttribute()));
        FunctionUtils.doIfNotBlank(clientProperties.getCssClass(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS, clientProperties.getCssClass()));
        FunctionUtils.doIfNotBlank(clientProperties.getDisplayName(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, clientProperties.getDisplayName()));

        val callbackUrl = StringUtils.defaultIfBlank(clientProperties.getCallbackUrl(), casProperties.getServer().getLoginUrl());
        client.setCallbackUrl(callbackUrl);
        LOGGER.trace("Client [{}] will use the callback URL [{}]", client.getName(), callbackUrl);

        switch (clientProperties.getCallbackUrlType()) {
            case PATH_PARAMETER -> client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
            case NONE -> client.setCallbackUrlResolver(new NoParameterCallbackUrlResolver());
            case QUERY_PARAMETER -> client.setCallbackUrlResolver(new QueryParameterCallbackUrlResolver());
        }
        customizers.forEach(customizer -> customizer.customize(client));
        if (!givenProperties.getAuthn().getPac4j().getCore().isLazyInit()) {
            client.init();
        }
    }

    protected Collection<IndirectClient> buildFoursquareIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val foursquare = pac4jProperties.getFoursquare();
        if (foursquare.isEnabled() && StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
            val client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
            configureClient(client, foursquare, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildGoogleIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val google = pac4jProperties.getGoogle();
        if (google.isEnabled() && StringUtils.isNotBlank(google.getId()) && StringUtils.isNotBlank(google.getSecret())) {
            val client = new Google2Client(google.getId(), google.getSecret());
            configureClient(client, google, casProperties);
            if (StringUtils.isNotBlank(google.getScope())) {
                client.setScope(Google2Client.Google2Scope.valueOf(google.getScope().toUpperCase(Locale.ENGLISH)));
            }

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildFacebookIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val fb = pac4jProperties.getFacebook();
        if (fb.isEnabled() && StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
            val client = new FacebookClient(fb.getId(), fb.getSecret());
            configureClient(client, fb, casProperties);

            FunctionUtils.doIfNotBlank(fb.getScope(), __ -> client.setScope(fb.getScope()));


            FunctionUtils.doIfNotBlank(fb.getFields(), __ -> client.setFields(fb.getFields()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildLinkedInIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val ln = pac4jProperties.getLinkedIn();
        if (ln.isEnabled() && StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            val client = new LinkedIn2Client(ln.getId(), ln.getSecret());
            configureClient(client, ln, casProperties);

            FunctionUtils.doIfNotBlank(ln.getScope(), __ -> client.setScope(ln.getScope()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildGitHubIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val github = pac4jProperties.getGithub();
        if (github.isEnabled() && StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            val client = new GitHubClient(github.getId(), github.getSecret());
            configureClient(client, github, casProperties);

            FunctionUtils.doIfNotBlank(github.getScope(), __ -> client.setScope(github.getScope()));
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildDropBoxIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val db = pac4jProperties.getDropbox();
        if (db.isEnabled() && StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            val client = new DropBoxClient(db.getId(), db.getSecret());
            configureClient(client, db, casProperties);
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildWindowsLiveIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val live = pac4jProperties.getWindowsLive();
        if (live.isEnabled() && StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
            val client = new WindowsLiveClient(live.getId(), live.getSecret());
            configureClient(client, live, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }


    protected Collection<IndirectClient> buildYahooIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val yahoo = pac4jProperties.getYahoo();
        if (yahoo.isEnabled() && StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
            val client = new YahooClient(yahoo.getId(), yahoo.getSecret());
            configureClient(client, yahoo, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildHiOrgServerIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val hiOrgServer = pac4jProperties.getHiOrgServer();
        if (hiOrgServer.isEnabled() && StringUtils.isNotBlank(hiOrgServer.getId()) && StringUtils.isNotBlank(hiOrgServer.getSecret())) {
            val client = new HiOrgServerClient(hiOrgServer.getId(), hiOrgServer.getSecret());
            configureClient(client, hiOrgServer, casProperties);
            if (StringUtils.isNotBlank(hiOrgServer.getScope())) {
                client.getConfiguration().setScope(hiOrgServer.getScope());
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildOAuth20IdentityProviders(final CasConfigurationProperties casProperties) {
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
                client.setKey(oauth.getId());
                client.setSecret(oauth.getSecret());
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
                configureClient(client, oauth, casProperties);
                LOGGER.debug("Created client [{}]", client);
                return client;
            })
            .collect(Collectors.toList());
    }

    protected Collection<IndirectClient> buildOidcIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getOidc()
            .stream()
            .map(oidc -> getOidcClientFrom(oidc, casProperties))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    protected Collection<IndirectClient> buildWordpressIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val wp = pac4jProperties.getWordpress();
        if (wp.isEnabled() && StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
            val client = new WordPressClient(wp.getId(), wp.getSecret());
            configureClient(client, wp, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    protected Collection<IndirectClient> buildTwitterIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val twitter = pac4jProperties.getTwitter();
        if (twitter.isEnabled() && StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
            val client = new TwitterClient(twitter.getId(), twitter.getSecret(), twitter.isIncludeEmail());
            configureClient(client, twitter, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    private OidcClient getOidcClientFrom(final Pac4jOidcClientProperties clientProperties,
                                         final CasConfigurationProperties casProperties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        if (clientProperties.getAzure().isEnabled() && StringUtils.isNotBlank(clientProperties.getAzure().getId())) {
            LOGGER.debug("Building OpenID Connect client for Azure AD...");
            val azure = getOidcConfigurationForClient(clientProperties.getAzure(), AzureAd2OidcConfiguration.class);
            azure.setTenant(resolver.resolve(clientProperties.getAzure().getTenant()));
            val cfg = new AzureAd2OidcConfiguration(azure);
            val azureClient = new AzureAd2Client(cfg);
            configureClient(azureClient, clientProperties.getAzure(), casProperties);
            return azureClient;
        }
        if (clientProperties.getGoogle().isEnabled() && StringUtils.isNotBlank(clientProperties.getGoogle().getId())) {
            LOGGER.debug("Building OpenID Connect client for Google...");
            val cfg = getOidcConfigurationForClient(clientProperties.getGoogle(), OidcConfiguration.class);
            val googleClient = new GoogleOidcClient(cfg);
            configureClient(googleClient, clientProperties.getGoogle(), casProperties);
            return googleClient;
        }
        if (clientProperties.getKeycloak().isEnabled() && StringUtils.isNotBlank(clientProperties.getKeycloak().getId())) {
            LOGGER.debug("Building OpenID Connect client for KeyCloak...");
            val cfg = getOidcConfigurationForClient(clientProperties.getKeycloak(), KeycloakOidcConfiguration.class);
            cfg.setRealm(resolver.resolve(clientProperties.getKeycloak().getRealm()));
            cfg.setBaseUri(resolver.resolve(clientProperties.getKeycloak().getBaseUri()));
            val kc = new KeycloakOidcClient(cfg);
            configureClient(kc, clientProperties.getKeycloak(), casProperties);
            return kc;
        }
        if (clientProperties.getApple().isEnabled() && StringUtils.isNotBlank(clientProperties.getApple().getPrivateKey())) {
            LOGGER.debug("Building OpenID Connect client for Apple...");
            val cfg = getOidcConfigurationForClient(clientProperties.getApple(), AppleOidcConfiguration.class);

            FunctionUtils.doUnchecked(__ -> {
                val factory = new PrivateKeyFactoryBean();
                factory.setAlgorithm("EC");
                factory.setSingleton(false);
                factory.setLocation(ResourceUtils.getResourceFrom(clientProperties.getApple().getPrivateKey()));
                cfg.setPrivateKey((ECPrivateKey) factory.getObject());
            });

            cfg.setPrivateKeyID(clientProperties.getApple().getPrivateKeyId());
            cfg.setTeamID(clientProperties.getApple().getTeamId());
            cfg.setTimeout(Beans.newDuration(clientProperties.getApple().getTimeout()));

            val kc = new AppleClient(cfg);
            configureClient(kc, clientProperties.getApple(), casProperties);
            return kc;
        }

        if (clientProperties.getGeneric().isEnabled()) {
            LOGGER.debug("Building generic OpenID Connect client...");
            val generic = getOidcConfigurationForClient(clientProperties.getGeneric(), OidcConfiguration.class);
            val oidcClient = new OidcClient(generic);
            configureClient(oidcClient, clientProperties.getGeneric(), casProperties);
            return oidcClient;
        }
        return null;
    }


    protected Collection<IndirectClient> buildPaypalIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val paypal = pac4jProperties.getPaypal();
        if (paypal.isEnabled() && StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            val client = new PayPalClient(paypal.getId(), paypal.getSecret());
            configureClient(client, paypal, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }

    private <T extends OidcConfiguration> T getOidcConfigurationForClient(final BasePac4jOidcClientProperties oidc,
                                                                          final Class<T> clazz) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        val cfg = FunctionUtils.doUnchecked(() -> clazz.getDeclaredConstructor().newInstance());
        FunctionUtils.doIfNotBlank(oidc.getScope(), __ -> cfg.setScope(resolver.resolve(oidc.getScope())));

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

        FunctionUtils.doIfNotBlank(oidc.getResponseMode(), __ -> cfg.setResponseMode(oidc.getResponseMode()));
        FunctionUtils.doIfNotBlank(oidc.getResponseType(), __ -> cfg.setResponseType(oidc.getResponseType()));

        if (!oidc.getMappedClaims().isEmpty()) {
            cfg.setMappedClaims(CollectionUtils.convertDirectedListToMap(oidc.getMappedClaims()));
        }
        cfg.setSslSocketFactory(casSSLContext.getSslContext().getSocketFactory());
        cfg.setHostnameVerifier(casSSLContext.getHostnameVerifier());
        return cfg;
    }

    protected Collection<IndirectClient> buildSaml2IdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getSaml()
            .stream()
            .filter(saml -> saml.isEnabled()
                && StringUtils.isNotBlank(saml.getMetadata().getIdentityProviderMetadataPath())
                && StringUtils.isNotBlank(saml.getServiceProviderEntityId()))
            .map(saml -> {
                val keystorePath = SpringExpressionLanguageValueResolver.getInstance().resolve(
                     StringUtils.defaultIfBlank(saml.getKeystorePath(), Beans.getTempFilePath("samlSpKeystore", ".jks")));
                val identityProviderMetadataPath = SpringExpressionLanguageValueResolver.getInstance()
                    .resolve(saml.getMetadata().getIdentityProviderMetadataPath());

                val cfg = new SAML2Configuration(keystorePath, saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), identityProviderMetadataPath);
                cfg.setForceKeystoreGeneration(saml.isForceKeystoreGeneration());

                FunctionUtils.doIf(saml.getCertificateExpirationDays() > 0,
                    __ -> cfg.setCertificateExpirationPeriod(Period.ofDays(saml.getCertificateExpirationDays()))).accept(saml);
                FunctionUtils.doIfNotNull(saml.getResponseBindingType(), cfg::setResponseBindingType);
                FunctionUtils.doIfNotNull(saml.getCertificateSignatureAlg(), cfg::setCertificateSignatureAlg);

                cfg.setPartialLogoutTreatedAsSuccess(saml.isPartialLogoutAsSuccess());
                cfg.setResponseDestinationAttributeMandatory(saml.isResponseDestinationMandatory());
                cfg.setSupportedProtocols(saml.getSupportedProtocols());

                FunctionUtils.doIfNotBlank(saml.getRequestInitiatorUrl(), __ -> cfg.setRequestInitiatorUrl(saml.getRequestInitiatorUrl()));
                FunctionUtils.doIfNotBlank(saml.getSingleLogoutServiceUrl(), __ -> cfg.setSingleSignOutServiceUrl(saml.getSingleLogoutServiceUrl()));
                FunctionUtils.doIfNotBlank(saml.getLogoutResponseBindingType(), __ -> cfg.setSpLogoutResponseBindingType(saml.getLogoutResponseBindingType()));

                cfg.setCertificateNameToAppend(StringUtils.defaultIfBlank(saml.getCertificateNameToAppend(), saml.getClientName()));
                cfg.setMaximumAuthenticationLifetime(Beans.newDuration(saml.getMaximumAuthenticationLifetime()).toSeconds());
                val serviceProviderEntityId = SpringExpressionLanguageValueResolver.getInstance().resolve(saml.getServiceProviderEntityId());
                cfg.setServiceProviderEntityId(serviceProviderEntityId);

                val samlSpMetadata = StringUtils.defaultIfBlank(saml.getMetadata().getServiceProvider().getFileSystem().getLocation(),
                    Beans.getTempFilePath("samlSpMetadata", ".xml"));
                FunctionUtils.doIfNotNull(samlSpMetadata, location -> {
                    val resource = ResourceUtils.getRawResourceFrom(location);
                    cfg.setServiceProviderMetadataResource(resource);
                });

                cfg.setAuthnRequestBindingType(saml.getDestinationBinding());
                cfg.setSpLogoutRequestBindingType(saml.getLogoutRequestBinding());
                cfg.setForceAuth(saml.isForceAuth());
                cfg.setPassive(saml.isPassive());
                cfg.setSignMetadata(saml.isSignServiceProviderMetadata());
                cfg.setMetadataSigner(new DefaultSAML2MetadataSigner(cfg));
                cfg.setAuthnRequestSigned(saml.isSignAuthnRequest());
                cfg.setSpLogoutRequestSigned(saml.isSignServiceProviderLogoutRequest());
                cfg.setAcceptedSkew(Beans.newDuration(saml.getAcceptedSkew()).toSeconds());
                cfg.setSslSocketFactory(casSSLContext.getSslContext().getSocketFactory());
                cfg.setHostnameVerifier(casSSLContext.getHostnameVerifier());

                FunctionUtils.doIfNotBlank(saml.getPrincipalIdAttribute(), __ -> cfg.setAttributeAsId(saml.getPrincipalIdAttribute()));
                FunctionUtils.doIfNotBlank(saml.getNameIdAttribute(), __ -> cfg.setNameIdAttribute(saml.getNameIdAttribute()));

                cfg.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                cfg.setWantsResponsesSigned(saml.isWantsResponsesSigned());
                cfg.setAllSignatureValidationDisabled(saml.isAllSignatureValidationDisabled());
                cfg.setUseNameQualifier(saml.isUseNameQualifier());
                cfg.setAttributeConsumingServiceIndex(saml.getAttributeConsumingServiceIndex());

                Optional.ofNullable(samlMessageStoreFactory.getIfAvailable())
                    .ifPresentOrElse(cfg::setSamlMessageStoreFactory, () -> {
                        FunctionUtils.doIf("EMPTY".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> cfg.setSamlMessageStoreFactory(new EmptyStoreFactory())).accept(saml);
                        FunctionUtils.doIf("SESSION".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> cfg.setSamlMessageStoreFactory(new HttpSessionStoreFactory())).accept(saml);
                        if (saml.getMessageStoreFactory().contains(".")) {
                            FunctionUtils.doAndHandle(__ -> {
                                val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getMessageStoreFactory());
                                val factory = (SAMLMessageStoreFactory) clazz.getDeclaredConstructor().newInstance();
                                cfg.setSamlMessageStoreFactory(factory);
                            });
                        }
                    });

                FunctionUtils.doIf(saml.getAssertionConsumerServiceIndex() >= 0,
                    __ -> cfg.setAssertionConsumerServiceIndex(saml.getAssertionConsumerServiceIndex())).accept(saml);

                if (!saml.getAuthnContextClassRef().isEmpty()) {
                    cfg.setComparisonType(saml.getAuthnContextComparisonType().toUpperCase(Locale.ENGLISH));
                    cfg.setAuthnContextClassRefs(saml.getAuthnContextClassRef());
                }

                FunctionUtils.doIfNotBlank(saml.getNameIdPolicyFormat(), __ -> cfg.setNameIdPolicyFormat(saml.getNameIdPolicyFormat()));

                if (!saml.getRequestedAttributes().isEmpty()) {
                    saml.getRequestedAttributes().stream()
                        .map(attribute -> new SAML2ServiceProviderRequestedAttribute(attribute.getName(), attribute.getFriendlyName(),
                            attribute.getNameFormat(), attribute.isRequired()))
                        .forEach(attribute -> cfg.getRequestedServiceProviderAttributes().add(attribute));
                }

                if (!saml.getBlockedSignatureSigningAlgorithms().isEmpty()) {
                    cfg.setBlackListedSignatureSigningAlgorithms(saml.getBlockedSignatureSigningAlgorithms());
                }
                if (!saml.getSignatureAlgorithms().isEmpty()) {
                    cfg.setSignatureAlgorithms(saml.getSignatureAlgorithms());
                }
                if (!saml.getSignatureReferenceDigestMethods().isEmpty()) {
                    cfg.setSignatureReferenceDigestMethods(saml.getSignatureReferenceDigestMethods());
                }

                FunctionUtils.doIfNotBlank(saml.getSignatureCanonicalizationAlgorithm(),
                    __ -> cfg.setSignatureCanonicalizationAlgorithm(saml.getSignatureCanonicalizationAlgorithm()));
                cfg.setProviderName(saml.getProviderName());
                cfg.setNameIdPolicyAllowCreate(saml.getNameIdPolicyAllowCreate().toBoolean());

                if (StringUtils.isNotBlank(saml.getSaml2AttributeConverter())) {
                    if (ScriptingUtils.isExternalGroovyScript(saml.getSaml2AttributeConverter())) {
                        FunctionUtils.doAndHandle(__ -> {
                            val resource = ResourceUtils.getResourceFrom(saml.getSaml2AttributeConverter());
                            val script = new WatchableGroovyScriptResource(resource);
                            cfg.setSamlAttributeConverter(new GroovyAttributeConverter(script));
                        });
                    } else {
                        FunctionUtils.doAndHandle(__ -> {
                            val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getSaml2AttributeConverter());
                            val converter = (AttributeConverter) clazz.getDeclaredConstructor().newInstance();
                            cfg.setSamlAttributeConverter(converter);
                        });
                    }
                }

                val mappedAttributes = saml.getMappedAttributes();
                if (!mappedAttributes.isEmpty()) {
                    cfg.setMappedAttributes(CollectionUtils.convertDirectedListToMap(mappedAttributes));
                }

                val client = new SAML2Client(cfg);
                configureClient(client, saml, casProperties);

                LOGGER.debug("Created delegated client [{}]", client);
                return client;
            })
            .collect(Collectors.toList());
    }

    protected Collection<IndirectClient> buildBitBucketIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val bitbucket = pac4jProperties.getBitbucket();
        if (bitbucket.isEnabled() && StringUtils.isNotBlank(bitbucket.getId()) && StringUtils.isNotBlank(bitbucket.getSecret())) {
            val client = new BitbucketClient(bitbucket.getId(), bitbucket.getSecret());
            configureClient(client, bitbucket, casProperties);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            return List.of(client);
        }
        return List.of();
    }


    protected Collection<IndirectClient> buildCasIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getCas()
            .stream()
            .filter(cas -> cas.isEnabled() && StringUtils.isNotBlank(cas.getLoginUrl()))
            .map(cas -> {
                val cfg = new CasConfiguration(cas.getLoginUrl(), CasProtocol.valueOf(cas.getProtocol()));
                val prefix = PATTERN_LOGIN_URL.matcher(cas.getLoginUrl()).replaceFirst("/");
                cfg.setPrefixUrl(StringUtils.appendIfMissing(prefix, "/"));
                cfg.setHostnameVerifier(casSSLContext.getHostnameVerifier());
                cfg.setSslSocketFactory(casSSLContext.getSslContext().getSocketFactory());

                val client = new CasClient(cfg);
                configureClient(client, cas, casProperties);
                LOGGER.debug("Created client [{}]", client);
                return client;
            })
            .collect(Collectors.toList());
    }

    protected Set<IndirectClient> buildAllIdentityProviders(final CasConfigurationProperties properties) {
        val newClients = new LinkedHashSet<IndirectClient>();
        newClients.addAll(buildCasIdentityProviders(properties));
        newClients.addAll(buildFacebookIdentityProviders(properties));
        newClients.addAll(buildOidcIdentityProviders(properties));
        newClients.addAll(buildOAuth20IdentityProviders(properties));
        newClients.addAll(buildSaml2IdentityProviders(properties));
        newClients.addAll(buildTwitterIdentityProviders(properties));
        newClients.addAll(buildDropBoxIdentityProviders(properties));
        newClients.addAll(buildFoursquareIdentityProviders(properties));
        newClients.addAll(buildGitHubIdentityProviders(properties));
        newClients.addAll(buildGoogleIdentityProviders(properties));
        newClients.addAll(buildWindowsLiveIdentityProviders(properties));
        newClients.addAll(buildYahooIdentityProviders(properties));
        newClients.addAll(buildLinkedInIdentityProviders(properties));
        newClients.addAll(buildPaypalIdentityProviders(properties));
        newClients.addAll(buildWordpressIdentityProviders(properties));
        newClients.addAll(buildBitBucketIdentityProviders(properties));
        newClients.addAll(buildHiOrgServerIdentityProviders(properties));
        return newClients;
    }
}
