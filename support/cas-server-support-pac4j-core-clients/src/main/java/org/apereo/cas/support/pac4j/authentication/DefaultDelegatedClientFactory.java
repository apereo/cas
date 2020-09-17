package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.BasePac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.RandomUtils;

import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
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
import org.pac4j.saml.store.SAMLMessageStoreFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class DefaultDelegatedClientFactory implements DelegatedClientFactory<IndirectClient> {
    private final CasConfigurationProperties casProperties;
    private final Collection<DelegatedClientFactoryCustomizer> customizers;

    @SneakyThrows
    private static <T extends OidcConfiguration> T getOidcConfigurationForClient(final BasePac4jOidcClientProperties oidc, final Class<T> clazz) {
        val cfg = clazz.getDeclaredConstructor().newInstance();
        if (StringUtils.isNotBlank(oidc.getScope())) {
            cfg.setScope(oidc.getScope());
        }
        cfg.setUseNonce(oidc.isUseNonce());
        cfg.setDisablePkce(oidc.isDisablePkce());
        cfg.setSecret(oidc.getSecret());
        cfg.setClientId(oidc.getId());
        cfg.setReadTimeout((int) Beans.newDuration(oidc.getReadTimeout()).toMillis());
        cfg.setConnectTimeout((int) Beans.newDuration(oidc.getConnectTimeout()).toMillis());
        if (StringUtils.isNotBlank(oidc.getPreferredJwsAlgorithm())) {
            cfg.setPreferredJwsAlgorithm(JWSAlgorithm.parse(oidc.getPreferredJwsAlgorithm().toUpperCase()));
        }
        cfg.setMaxClockSkew(oidc.getMaxClockSkew());
        cfg.setDiscoveryURI(oidc.getDiscoveryUri());
        cfg.setCustomParams(oidc.getCustomParams());
        cfg.setLogoutUrl(oidc.getLogoutUrl());

        cfg.setExpireSessionWithToken(oidc.isExpireSessionWithToken());
        if (StringUtils.isNotBlank(oidc.getTokenExpirationAdvance())) {
            cfg.setTokenExpirationAdvance((int) Beans.newDuration(oidc.getTokenExpirationAdvance()).toSeconds());
        }

        if (StringUtils.isNotBlank(oidc.getResponseMode())) {
            cfg.setResponseMode(oidc.getResponseMode());
        }
        if (StringUtils.isNotBlank(oidc.getResponseType())) {
            cfg.setResponseType(oidc.getResponseType());
        }
        return cfg;
    }

    /**
     * Configure github client.
     *
     * @param properties the properties
     */
    protected void configureGitHubClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val github = pac4jProperties.getGithub();
        if (github.isEnabled() && StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            val client = new GitHubClient(github.getId(), github.getSecret());
            configureClient(client, github);
            if (StringUtils.isNotBlank(github.getScope())) {
                client.setScope(github.getScope());
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure dropbox client.
     *
     * @param properties the properties
     */
    protected void configureDropBoxClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val db = pac4jProperties.getDropbox();
        if (db.isEnabled() && StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
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
    protected void configureOrcidClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val db = pac4jProperties.getOrcid();
        if (db.isEnabled() && StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
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
    protected void configureWindowsLiveClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val live = pac4jProperties.getWindowsLive();
        if (live.isEnabled() && StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
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
    protected void configureYahooClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val yahoo = pac4jProperties.getYahoo();
        if (yahoo.isEnabled() && StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
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
    protected void configureFoursquareClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val foursquare = pac4jProperties.getFoursquare();
        if (foursquare.isEnabled() && StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
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
    protected void configureGoogleClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val google = pac4jProperties.getGoogle();
        if (google.isEnabled() && StringUtils.isNotBlank(google.getId()) && StringUtils.isNotBlank(google.getSecret())) {
            val client = new Google2Client(google.getId(), google.getSecret());
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
    protected void configureFacebookClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val fb = pac4jProperties.getFacebook();
        if (fb.isEnabled() && StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
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
    protected void configureLinkedInClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val ln = pac4jProperties.getLinkedIn();
        if (ln.isEnabled() && StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            val client = new LinkedIn2Client(ln.getId(), ln.getSecret());
            configureClient(client, ln);
            if (StringUtils.isNotBlank(ln.getScope())) {
                client.setScope(ln.getScope());
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
    protected void configureHiOrgServerClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val hiOrgServer = pac4jProperties.getHiOrgServer();
        if (hiOrgServer.isEnabled() && StringUtils.isNotBlank(hiOrgServer.getId()) && StringUtils.isNotBlank(hiOrgServer.getSecret())) {
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
    protected void configureTwitterClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val twitter = pac4jProperties.getTwitter();
        if (twitter.isEnabled() && StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
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
    protected void configureWordPressClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val wp = pac4jProperties.getWordpress();
        if (wp.isEnabled() && StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
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
    protected void configureBitBucketClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val bb = pac4jProperties.getBitbucket();
        if (bb.isEnabled() && StringUtils.isNotBlank(bb.getId()) && StringUtils.isNotBlank(bb.getSecret())) {
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
    protected void configurePayPalClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val paypal = pac4jProperties.getPaypal();
        if (paypal.isEnabled() && StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            val client = new PayPalClient(paypal.getId(), paypal.getSecret());
            configureClient(client, paypal);

            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    /**
     * Configure cas client.
     *
     * @param properties the properties
     */
    protected void configureCasClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val index = new AtomicInteger();
        pac4jProperties.getCas()
            .stream()
            .filter(cas -> cas.isEnabled() && StringUtils.isNotBlank(cas.getLoginUrl()))
            .forEach(cas -> {
                val cfg = new CasConfiguration(cas.getLoginUrl(), CasProtocol.valueOf(cas.getProtocol()));
                val prefix = cas.getLoginUrl().replaceFirst("/login$", "/");
                cfg.setPrefixUrl(StringUtils.appendIfMissing(prefix, "/"));
                val client = new CasClient(cfg);

                if (StringUtils.isBlank(cas.getClientName())) {
                    val count = index.intValue();
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
    protected void configureSamlClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val index = new AtomicInteger();
        pac4jProperties.getSaml()
            .stream()
            .filter(saml -> saml.isEnabled()
                && StringUtils.isNotBlank(saml.getKeystorePath())
                && StringUtils.isNotBlank(saml.getIdentityProviderMetadataPath())
                && StringUtils.isNotBlank(saml.getServiceProviderEntityId())
                && StringUtils.isNotBlank(saml.getServiceProviderMetadataPath()))
            .forEach(saml -> {
                val cfg = new SAML2Configuration(saml.getKeystorePath(),
                    saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), saml.getIdentityProviderMetadataPath());

                cfg.setForceKeystoreGeneration(saml.isForceKeystoreGeneration());
                cfg.setCertificateNameToAppend(StringUtils.defaultIfBlank(saml.getCertificateNameToAppend(), saml.getClientName()));
                cfg.setMaximumAuthenticationLifetime(saml.getMaximumAuthenticationLifetime());
                cfg.setServiceProviderEntityId(saml.getServiceProviderEntityId());
                cfg.setServiceProviderMetadataPath(saml.getServiceProviderMetadataPath());
                cfg.setAuthnRequestBindingType(saml.getDestinationBinding());
                cfg.setForceAuth(saml.isForceAuth());
                cfg.setPassive(saml.isPassive());
                cfg.setSignMetadata(saml.isSignServiceProviderMetadata());
                cfg.setAuthnRequestSigned(saml.isSignAuthnRequest());
                cfg.setSpLogoutRequestSigned(saml.isSignServiceProviderLogoutRequest());
                cfg.setAcceptedSkew(saml.getAcceptedSkew());

                if (StringUtils.isNotBlank(saml.getPrincipalIdAttribute())) {
                    cfg.setAttributeAsId(saml.getPrincipalIdAttribute());
                }
                cfg.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                cfg.setWantsResponsesSigned(saml.isWantsResponsesSigned());
                cfg.setAllSignatureValidationDisabled(saml.isAllSignatureValidationDisabled());
                cfg.setUseNameQualifier(saml.isUseNameQualifier());
                cfg.setAttributeConsumingServiceIndex(saml.getAttributeConsumingServiceIndex());

                try {
                    val clazz = ClassUtils.getClass(
                        DefaultDelegatedClientFactory.class.getClassLoader(), saml.getMessageStoreFactory());
                    cfg.setSamlMessageStoreFactory(
                        SAMLMessageStoreFactory.class.cast(clazz.getDeclaredConstructor().newInstance()));
                } catch (final Exception e) {
                    LOGGER.error("Unable to instantiate message store factory class [{}]", saml.getMessageStoreFactory(), e);
                }

                if (saml.getAssertionConsumerServiceIndex() >= 0) {
                    cfg.setAssertionConsumerServiceIndex(saml.getAssertionConsumerServiceIndex());
                }
                if (!saml.getAuthnContextClassRef().isEmpty()) {
                    cfg.setComparisonType(saml.getAuthnContextComparisonType().toUpperCase());
                    cfg.setAuthnContextClassRefs(saml.getAuthnContextClassRef());
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

                if (!saml.getBlackListedSignatureSigningAlgorithms().isEmpty()) {
                    cfg.setBlackListedSignatureSigningAlgorithms(saml.getBlackListedSignatureSigningAlgorithms());
                }
                if (!saml.getSignatureAlgorithms().isEmpty()) {
                    cfg.setSignatureAlgorithms(saml.getSignatureAlgorithms());
                }
                if (!saml.getSignatureReferenceDigestMethods().isEmpty()) {
                    cfg.setSignatureReferenceDigestMethods(saml.getSignatureReferenceDigestMethods());
                }
                if (!StringUtils.isNotBlank(saml.getSignatureCanonicalizationAlgorithm())) {
                    cfg.setSignatureCanonicalizationAlgorithm(saml.getSignatureCanonicalizationAlgorithm());
                }
                cfg.setProviderName(saml.getProviderName());
                cfg.setNameIdPolicyAllowCreate(saml.getNameIdPolicyAllowCreate().toBoolean());

                val mappedAttributes = saml.getMappedAttributes();
                if (!mappedAttributes.isEmpty()) {
                    val results = mappedAttributes
                        .stream()
                        .collect(Collectors.toMap(Pac4jSamlClientProperties.ServiceProviderMappedAttribute::getName,
                            Pac4jSamlClientProperties.ServiceProviderMappedAttribute::getMappedTo));
                    cfg.setMappedAttributes(results);
                }

                val client = new SAML2Client(cfg);

                if (StringUtils.isBlank(saml.getClientName())) {
                    val count = index.intValue();
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
    protected void configureOAuth20Client(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val index = new AtomicInteger();
        pac4jProperties.getOauth2()
            .stream()
            .filter(oauth -> oauth.isEnabled()
                && StringUtils.isNotBlank(oauth.getId())
                && StringUtils.isNotBlank(oauth.getSecret()))
            .forEach(oauth -> {
                val client = new GenericOAuth20Client();
                client.setProfileId(StringUtils.defaultIfBlank(oauth.getPrincipalAttributeId(), pac4jProperties.getPrincipalAttributeId()));
                client.setKey(oauth.getId());
                client.setSecret(oauth.getSecret());
                client.setProfileAttrs(oauth.getProfileAttrs());
                client.setProfileNodePath(oauth.getProfilePath());
                client.setProfileUrl(oauth.getProfileUrl());
                client.setProfileVerb(Verb.valueOf(oauth.getProfileVerb().toUpperCase()));
                client.setTokenUrl(oauth.getTokenUrl());
                client.setAuthUrl(oauth.getAuthUrl());
                client.setScope(oauth.getScope());
                client.setCustomParams(oauth.getCustomParams());
                client.getConfiguration().setResponseType(oauth.getResponseType());

                if (StringUtils.isBlank(oauth.getClientName())) {
                    val count = index.intValue();
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
    protected void configureOidcClient(final Collection<IndirectClient> properties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        pac4jProperties.getOidc()
            .forEach(oidc -> {
                val client = getOidcClientFrom(oidc);
                if (client != null) {
                    LOGGER.debug("Created client [{}]", client);
                    properties.add(client);
                }
            });
    }

    private OidcClient getOidcClientFrom(final Pac4jOidcClientProperties oidc) {
        if (oidc.getAzure().isEnabled() && StringUtils.isNotBlank(oidc.getAzure().getId())) {
            LOGGER.debug("Building OpenID Connect client for Azure AD...");
            val azure = getOidcConfigurationForClient(oidc.getAzure(), AzureAdOidcConfiguration.class);
            azure.setTenant(oidc.getAzure().getTenant());
            val cfg = new AzureAdOidcConfiguration(azure);
            val azureClient = new AzureAdClient(cfg);
            configureClient(azureClient, oidc.getAzure());
            return azureClient;
        }
        if (oidc.getGoogle().isEnabled() && StringUtils.isNotBlank(oidc.getGoogle().getId())) {
            LOGGER.debug("Building OpenID Connect client for Google...");
            val cfg = getOidcConfigurationForClient(oidc.getGoogle(), OidcConfiguration.class);
            val googleClient = new GoogleOidcClient(cfg);
            configureClient(googleClient, oidc.getGoogle());
            return googleClient;
        }
        if (oidc.getKeycloak().isEnabled() && StringUtils.isNotBlank(oidc.getKeycloak().getId())) {
            LOGGER.debug("Building OpenID Connect client for KeyCloak...");
            val cfg = getOidcConfigurationForClient(oidc.getKeycloak(), KeycloakOidcConfiguration.class);
            cfg.setRealm(oidc.getKeycloak().getRealm());
            cfg.setBaseUri(oidc.getKeycloak().getBaseUri());
            val kc = new KeycloakOidcClient(cfg);
            configureClient(kc, oidc.getKeycloak());
            return kc;
        }
        if (oidc.getGeneric().isEnabled()) {
            LOGGER.debug("Building generic OpenID Connect client...");
            val generic = getOidcConfigurationForClient(oidc.getGeneric(), OidcConfiguration.class);
            val oc = new OidcClient<>(generic);
            configureClient(oc, oidc.getGeneric());
            return oc;
        }
        return null;
    }

    /**
     * Sets client name.
     *
     * @param client the client
     * @param props  the props
     */
    protected void configureClient(final IndirectClient client, final Pac4jBaseClientProperties props) {
        val cname = props.getClientName();
        if (StringUtils.isNotBlank(cname)) {
            client.setName(cname);
        } else {
            val className = client.getClass().getSimpleName();
            val genName = className.concat(RandomUtils.randomNumeric(2));
            client.setName(genName);
            LOGGER.warn("Client name for [{}] is set to a generated value of [{}]. "
                + "Consider defining an explicit name for the delegated provider", className, genName);
        }
        val customProperties = client.getCustomProperties();
        customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, props.isAutoRedirect());
        if (StringUtils.isNotBlank(props.getPrincipalAttributeId())) {
            customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID, props.getPrincipalAttributeId());
        }
        if (StringUtils.isNotBlank(props.getCssClass())) {
            customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS, props.getCssClass());
        }
        client.setCallbackUrl(casProperties.getServer().getLoginUrl());
        switch (props.getCallbackUrlType()) {
            case PATH_PARAMETER:
                client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
                break;
            case NONE:
                client.setCallbackUrlResolver(new NoParameterCallbackUrlResolver());
                break;
            case QUERY_PARAMETER:
            default:
                client.setCallbackUrlResolver(new QueryParameterCallbackUrlResolver());
        }
        this.customizers.forEach(customizer -> customizer.customize(client));
        if (!casProperties.getAuthn().getPac4j().isLazyInit()) {
            client.init();
        }
    }

    @Override
    public Collection<IndirectClient> build() {
        val clients = new LinkedHashSet<IndirectClient>();

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
