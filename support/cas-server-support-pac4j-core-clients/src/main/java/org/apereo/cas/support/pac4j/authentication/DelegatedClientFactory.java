package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.BasePac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.support.pac4j.logout.CasServerSpecificLogoutHandler;

import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.oauth.client.BitbucketClient;
import org.pac4j.oauth.client.DropBoxClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.FoursquareClient;
import org.pac4j.oauth.client.GenericOAuth20Client;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.HiOrgServerClient;
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
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.metadata.SAML2ServiceProvicerRequestedAttribute;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class DelegatedClientFactory {
    /**
     * The Pac 4 j properties.
     */
    private final Pac4jDelegatedAuthenticationProperties pac4jProperties;

    /**
     * The pac4j specific logout handler for the CAS server.
     */
    private final LogoutHandler casServerSpecificLogoutHandler;

    public DelegatedClientFactory(final Pac4jDelegatedAuthenticationProperties pac4jProperties) {
        this(pac4jProperties, new CasServerSpecificLogoutHandler());
    }

    /**
     * Configure github client.
     *
     * @param properties the properties
     */
    protected void configureGitHubClient(final Collection<BaseClient> properties) {
        val github = pac4jProperties.getGithub();
        if (StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            val client = new GitHubClient(github.getId(), github.getSecret());
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
    protected void configureDropBoxClient(final Collection<BaseClient> properties) {
        val db = pac4jProperties.getDropbox();
        if (StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            val client = new DropBoxClient(db.getId(), db.getSecret());
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
        val db = pac4jProperties.getOrcid();
        if (StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            val client = new OrcidClient(db.getId(), db.getSecret());
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
        val live = pac4jProperties.getWindowsLive();
        if (StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
            val client = new WindowsLiveClient(live.getId(), live.getSecret());
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
        val yahoo = pac4jProperties.getYahoo();
        if (StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
            val client = new YahooClient(yahoo.getId(), yahoo.getSecret());
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
        val foursquare = pac4jProperties.getFoursquare();
        if (StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
            val client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
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
        val google = pac4jProperties.getGoogle();
        val client = new Google2Client(google.getId(), google.getSecret());
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
        val fb = pac4jProperties.getFacebook();
        if (StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
            val client = new FacebookClient(fb.getId(), fb.getSecret());

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
        val ln = pac4jProperties.getLinkedIn();
        if (StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            val client = new LinkedIn2Client(ln.getId(), ln.getSecret());
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
     * Configure HiOrg-Server client.
     *
     * @param properties the properties
     */
    protected void configureHiOrgServerClient(final Collection<BaseClient> properties) {
        val hiOrgServer = pac4jProperties.getHiOrgServer();
        if (StringUtils.isNotBlank(hiOrgServer.getId()) && StringUtils.isNotBlank(hiOrgServer.getSecret())) {
            val client = new HiOrgServerClient(hiOrgServer.getId(), hiOrgServer.getSecret());
            configureClient(client, hiOrgServer);
            if (StringUtils.isNotBlank(hiOrgServer.getScope())) {
                client.getConfiguration().setScope(hiOrgServer.getScope());
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
        val twitter = pac4jProperties.getTwitter();
        if (StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
            val client = new TwitterClient(twitter.getId(), twitter.getSecret(), twitter.isIncludeEmail());
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
    protected void configureWordPressClient(final Collection<BaseClient> properties) {
        val wp = pac4jProperties.getWordpress();
        if (StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
            val client = new WordPressClient(wp.getId(), wp.getSecret());
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
    protected void configureBitBucketClient(final Collection<BaseClient> properties) {
        val bb = pac4jProperties.getBitbucket();
        if (StringUtils.isNotBlank(bb.getId()) && StringUtils.isNotBlank(bb.getSecret())) {
            val client = new BitbucketClient(bb.getId(), bb.getSecret());
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
    protected void configurePayPalClient(final Collection<BaseClient> properties) {
        val paypal = pac4jProperties.getPaypal();
        if (StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            val client = new PayPalClient(paypal.getId(), paypal.getSecret());
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
        val cname = props.getClientName();
        if (StringUtils.isNotBlank(cname)) {
            client.setName(cname);
        } else {
            val className = client.getClass().getSimpleName();
            val genName = className.concat(RandomStringUtils.randomNumeric(2));
            client.setName(genName);
            LOGGER.warn("Client name for [{}] is set to a generated value of [{}]. "
                + "Consider defining an explicit name for the delegated provider", className, genName);
        }
        val customProperties = client.getCustomProperties();
        customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, props.isAutoRedirect());
        if (StringUtils.isNotBlank(props.getPrincipalAttributeId())) {
            customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID, props.getPrincipalAttributeId());
        }
    }

    /**
     * Configure cas client.
     *
     * @param properties the properties
     */
    protected void configureCasClient(final Collection<BaseClient> properties) {
        val index = new AtomicInteger();
        pac4jProperties.getCas()
            .stream()
            .filter(cas -> StringUtils.isNotBlank(cas.getLoginUrl()))
            .forEach(cas -> {
                val cfg = new CasConfiguration(cas.getLoginUrl(), CasProtocol.valueOf(cas.getProtocol()));
                cfg.setLogoutHandler(casServerSpecificLogoutHandler);
                val client = new CasClient(cfg);

                val count = index.intValue();
                if (StringUtils.isBlank(cas.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
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
        val index = new AtomicInteger();
        pac4jProperties.getSaml()
            .stream()
            .filter(saml -> StringUtils.isNotBlank(saml.getKeystorePath())
                && StringUtils.isNotBlank(saml.getIdentityProviderMetadataPath())
                && StringUtils.isNotBlank(saml.getServiceProviderEntityId())
                && StringUtils.isNotBlank(saml.getServiceProviderMetadataPath()))
            .forEach(saml -> {
                val cfg = new SAML2Configuration(saml.getKeystorePath(),
                    saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), saml.getIdentityProviderMetadataPath());
                cfg.setCertificateNameToAppend(StringUtils.defaultIfBlank(saml.getCertificateNameToAppend(), saml.getClientName()));
                cfg.setMaximumAuthenticationLifetime(saml.getMaximumAuthenticationLifetime());
                cfg.setServiceProviderEntityId(saml.getServiceProviderEntityId());
                cfg.setServiceProviderMetadataPath(saml.getServiceProviderMetadataPath());
                cfg.setAuthnRequestBindingType(saml.getDestinationBinding());
                cfg.setForceAuth(saml.isForceAuth());
                cfg.setPassive(saml.isPassive());
                cfg.setSignMetadata(saml.isSignServiceProviderMetadata());
                cfg.setAcceptedSkew(saml.getAcceptedSkew());

                if (StringUtils.isNotBlank(saml.getPrincipalIdAttribute())) {
                    cfg.setAttributeAsId(saml.getPrincipalIdAttribute());
                }
                cfg.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                cfg.setLogoutHandler(casServerSpecificLogoutHandler);
                cfg.setUseNameQualifier(saml.isUseNameQualifier());
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

                if (!saml.getRequestedAttributes().isEmpty()) {
                    saml.getRequestedAttributes().stream()
                        .map(attribute -> new SAML2ServiceProvicerRequestedAttribute(attribute.getName(), attribute.getFriendlyName(),
                            attribute.getNameFormat(), attribute.isRequired()))
                        .forEach(attribute -> cfg.getRequestedServiceProviderAttributes().add(attribute));
                }

                val mappedAttributes = saml.getMappedAttributes();
                if (!mappedAttributes.isEmpty()) {
                    val results = mappedAttributes
                        .stream()
                        .collect(Collectors.toMap(Pac4jSamlClientProperties.ServiceProviderMappedAttribute::getName,
                            Pac4jSamlClientProperties.ServiceProviderMappedAttribute::getMappedTo));
                    cfg.setMappedAttributes(results);
                }

                val client = new SAML2Client(cfg);

                val count = index.intValue();
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
     * Configure OAuth client.
     *
     * @param properties the properties
     */
    protected void configureOAuth20Client(final Collection<BaseClient> properties) {
        val index = new AtomicInteger();
        pac4jProperties.getOauth2()
            .stream()
            .filter(oauth -> StringUtils.isNotBlank(oauth.getId()) && StringUtils.isNotBlank(oauth.getSecret()))
            .forEach(oauth -> {
                val client = new GenericOAuth20Client();
                client.setKey(oauth.getId());
                client.setSecret(oauth.getSecret());
                client.setProfileAttrs(oauth.getProfileAttrs());
                client.setProfileNodePath(oauth.getProfilePath());
                client.setProfileUrl(oauth.getProfileUrl());
                client.setProfileVerb(Verb.valueOf(oauth.getProfileVerb().toUpperCase()));
                client.setTokenUrl(oauth.getTokenUrl());
                client.setAuthUrl(oauth.getAuthUrl());
                client.setCustomParams(oauth.getCustomParams());
                val count = index.intValue();
                if (StringUtils.isBlank(oauth.getClientName())) {
                    client.setName(client.getClass().getSimpleName() + count);
                }
                client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
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
        pac4jProperties.getOidc()
            .forEach(oidc -> {
                val client = getOidcClientFrom(oidc);
                LOGGER.debug("Created client [{}]", client);
                properties.add(client);
            });
    }

    private OidcClient getOidcClientFrom(final Pac4jOidcClientProperties oidc) {
        if (StringUtils.isNotBlank(oidc.getAzure().getId())) {
            LOGGER.debug("Building OpenID Connect client for Azure AD...");
            val azure = getOidcConfigurationForClient(oidc.getAzure(), AzureAdOidcConfiguration.class);
            azure.setTenant(oidc.getAzure().getTenant());
            val cfg = new AzureAdOidcConfiguration(azure);
            val azureClient = new AzureAdClient(cfg);
            configureClient(azureClient, oidc.getAzure());
            return azureClient;
        }
        if (StringUtils.isNotBlank(oidc.getGoogle().getId())) {
            LOGGER.debug("Building OpenID Connect client for Google...");
            val cfg = getOidcConfigurationForClient(oidc.getGoogle(), OidcConfiguration.class);
            val googleClient = new GoogleOidcClient(cfg);
            configureClient(googleClient, oidc.getGoogle());
            return googleClient;
        }
        if (StringUtils.isNotBlank(oidc.getKeycloak().getId())) {
            LOGGER.debug("Building OpenID Connect client for KeyCloak...");
            val cfg = getOidcConfigurationForClient(oidc.getKeycloak(), KeycloakOidcConfiguration.class);
            val kc = new KeycloakOidcClient(cfg);
            configureClient(kc, oidc.getKeycloak());
            return kc;
        }
        LOGGER.debug("Building generic OpenID Connect client...");
        val generic = getOidcConfigurationForClient(oidc.getGeneric(), OidcConfiguration.class);
        val oc = new OidcClient(generic);
        oc.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
        configureClient(oc, oidc.getGeneric());
        return oc;
    }

    @SneakyThrows
    private static <T extends OidcConfiguration> T getOidcConfigurationForClient(final BasePac4jOidcClientProperties oidc, final Class<T> clazz) {
        val cfg = clazz.getDeclaredConstructor().newInstance();
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
        
        if (StringUtils.isNotBlank(oidc.getResponseMode())) {
            cfg.setResponseMode(oidc.getResponseMode());
        }
        if (StringUtils.isNotBlank(oidc.getResponseType())) {
            cfg.setResponseType(oidc.getResponseType());
        }
        return cfg;
    }

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    public Set<BaseClient> build() {
        val clients = new LinkedHashSet<BaseClient>();

        configureCasClient(clients);
        configureFacebookClient(clients);
        configureOidcClient(clients);
        configureOAuth20Client(clients);
        configureSamlClient(clients);
        configureTwitterClient(clients);
        configureDropBoxClient(clients);
        configureFoursquareClient(clients);
        configureGitHubClient(clients);
        configureGoogleClient(clients);
        configureWindowsLiveClient(clients);
        configureYahooClient(clients);
        configureLinkedInClient(clients);
        configurePayPalClient(clients);
        configureWordPressClient(clients);
        configureBitBucketClient(clients);
        configureOrcidClient(clients);
        configureHiOrgServerClient(clients);

        return clients;
    }
}
