package org.apereo.cas.support.pac4j.config.support.authentication;

import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.apereo.cas.support.pac4j.web.flow.SAML2ClientLogoutAction;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.oauth.client.BitbucketClient;
import org.pac4j.oauth.client.DropBoxClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.FoursquareClient;
import org.pac4j.oauth.client.GenericOAuth20Client;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.LinkedIn2Client;
import org.pac4j.oauth.client.PayPalClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.WindowsLiveClient;
import org.pac4j.oauth.client.WordPressClient;
import org.pac4j.oauth.client.YahooClient;
import org.pac4j.oidc.client.AzureAdClient;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link Pac4jAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("pac4jAuthenticationEventExecutionPlanConfiguration")
public class Pac4jAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jAuthenticationEventExecutionPlanConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    private void configureGithubClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Github github = casProperties.getAuthn().getPac4j().getGithub();
        if (StringUtils.isNotBlank(github.getId()) && StringUtils.isNotBlank(github.getSecret())) {
            final GitHubClient client = new GitHubClient(github.getId(), github.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureDropboxClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Dropbox db = casProperties.getAuthn().getPac4j().getDropbox();
        if (StringUtils.isNotBlank(db.getId()) && StringUtils.isNotBlank(db.getSecret())) {
            final DropBoxClient client = new DropBoxClient(db.getId(), db.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureWindowsLiveClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.WindowsLive live = casProperties.getAuthn().getPac4j().getWindowsLive();
        if (StringUtils.isNotBlank(live.getId()) && StringUtils.isNotBlank(live.getSecret())) {
            final WindowsLiveClient client = new WindowsLiveClient(live.getId(), live.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureYahooClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Yahoo yahoo = casProperties.getAuthn().getPac4j().getYahoo();
        if (StringUtils.isNotBlank(yahoo.getId()) && StringUtils.isNotBlank(yahoo.getSecret())) {
            final YahooClient client = new YahooClient(yahoo.getId(), yahoo.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureFoursquareClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Foursquare foursquare = casProperties.getAuthn().getPac4j().getFoursquare();
        if (StringUtils.isNotBlank(foursquare.getId()) && StringUtils.isNotBlank(foursquare.getSecret())) {
            final FoursquareClient client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureGoogleClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Google google = casProperties.getAuthn().getPac4j().getGoogle();
        final Google2Client client = new Google2Client(google.getId(), google.getSecret());
        if (StringUtils.isNotBlank(google.getId()) && StringUtils.isNotBlank(google.getSecret())) {
            if (StringUtils.isNotBlank(google.getScope())) {
                client.setScope(Google2Client.Google2Scope.valueOf(google.getScope().toUpperCase()));
            }
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureFacebookClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Facebook fb = casProperties.getAuthn().getPac4j().getFacebook();
        if (StringUtils.isNotBlank(fb.getId()) && StringUtils.isNotBlank(fb.getSecret())) {
            final FacebookClient client = new FacebookClient(fb.getId(), fb.getSecret());
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

    private void configureLinkedInClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.LinkedIn ln = casProperties.getAuthn().getPac4j().getLinkedIn();
        if (StringUtils.isNotBlank(ln.getId()) && StringUtils.isNotBlank(ln.getSecret())) {
            final LinkedIn2Client client = new LinkedIn2Client(ln.getId(), ln.getSecret());

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

    private void configureTwitterClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Twitter twitter = casProperties.getAuthn().getPac4j().getTwitter();
        if (StringUtils.isNotBlank(twitter.getId()) && StringUtils.isNotBlank(twitter.getSecret())) {
            final TwitterClient client = new TwitterClient(twitter.getId(), twitter.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureWordpressClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Wordpress wp = casProperties.getAuthn().getPac4j().getWordpress();
        if (StringUtils.isNotBlank(wp.getId()) && StringUtils.isNotBlank(wp.getSecret())) {
            final WordPressClient client = new WordPressClient(wp.getId(), wp.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureBitbucketClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Bitbucket bb = casProperties.getAuthn().getPac4j().getBitbucket();
        if (StringUtils.isNotBlank(bb.getId()) && StringUtils.isNotBlank(bb.getSecret())) {
            final BitbucketClient client = new BitbucketClient(bb.getId(), bb.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configurePaypalClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Paypal paypal = casProperties.getAuthn().getPac4j().getPaypal();
        if (StringUtils.isNotBlank(paypal.getId()) && StringUtils.isNotBlank(paypal.getSecret())) {
            final PayPalClient client = new PayPalClient(paypal.getId(), paypal.getSecret());
            LOGGER.debug("Created client [{}] with identifier [{}]", client.getName(), client.getKey());
            properties.add(client);
        }
    }

    private void configureCasClient(final Collection<BaseClient> properties) {
        final AtomicInteger index = new AtomicInteger();
        casProperties.getAuthn().getPac4j().getCas()
                .stream()
                .filter(cas -> StringUtils.isNotBlank(cas.getLoginUrl()))
                .forEach(cas -> {
                    final CasConfiguration cfg = new CasConfiguration(cas.getLoginUrl(), cas.getProtocol());
                    final CasClient client = new CasClient(cfg);
                    final int count = index.intValue();
                    if (count > 0) {
                        client.setName(client.getClass().getSimpleName() + count);
                    }
                    index.incrementAndGet();
                    LOGGER.debug("Created client [{}]", client);
                    properties.add(client);
                });
    }

    private void configureSamlClient(final Collection<BaseClient> properties) {
        final AtomicInteger index = new AtomicInteger();
        casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> StringUtils.isNotBlank(saml.getKeystorePath()) && StringUtils.isNotBlank(saml.getIdentityProviderMetadataPath()))
                .forEach(saml -> {
                    final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration(saml.getKeystorePath(), saml.getKeystorePassword(),
                            saml.getPrivateKeyPassword(), saml.getIdentityProviderMetadataPath());
                    cfg.setMaximumAuthenticationLifetime(saml.getMaximumAuthenticationLifetime());
                    cfg.setServiceProviderEntityId(saml.getServiceProviderEntityId());
                    cfg.setServiceProviderMetadataPath(saml.getServiceProviderMetadataPath());
                    cfg.setDestinationBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                    final SAML2Client client = new SAML2Client(cfg);
                    
                    final int count = index.intValue();
                    if (count > 0) {
                        client.setName(client.getClass().getSimpleName() + count);
                    }
                    index.incrementAndGet();
                    LOGGER.debug("Created client [{}]", client);
                    properties.add(client);
                });
    }

    private void configureOAuth20Client(final Collection<BaseClient> properties) {
        final AtomicInteger index = new AtomicInteger();
        casProperties.getAuthn().getPac4j().getOauth2()
                .stream()
                .filter(oauth -> StringUtils.isNotBlank(oauth.getId()) && StringUtils.isNotBlank(oauth.getSecret()))
                .forEach(oauth -> {
                    final GenericOAuth20Client client = new GenericOAuth20Client();
                    client.setKey(oauth.getId());
                    client.setSecret(oauth.getSecret());
                    client.setProfileAttrs(oauth.getProfileAttrs());
                    client.setProfileNodePath(oauth.getProfilePath());
                    client.setProfileUrl(oauth.getProfileUrl());
                    client.setProfileVerb(Verb.valueOf(oauth.getProfileVerb().toUpperCase()));
                    client.setTokenUrl(oauth.getTokenUrl());
                    client.setAuthUrl(oauth.getAuthUrl());
                    client.setCustomParams(oauth.getCustomParams());
                    final int count = index.intValue();
                    if (count > 0) {
                        client.setName(client.getClass().getSimpleName() + count);
                    }
                    index.incrementAndGet();
                    LOGGER.debug("Created client [{}]", client);
                    properties.add(client);
                });
    }


    private void configureOidcClient(final Collection<BaseClient> properties) {
        final AtomicInteger index = new AtomicInteger();
        casProperties.getAuthn().getPac4j().getOidc()
                .stream()
                .filter(oidc -> StringUtils.isNotBlank(oidc.getId()) && StringUtils.isNotBlank(oidc.getSecret()))
                .forEach(oidc -> {

                    final OidcConfiguration cfg = new OidcConfiguration();
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

                    final OidcClient client;
                    switch (oidc.getType().toUpperCase()) {
                        case "GOOGLE":
                            client = new GoogleOidcClient(cfg);
                            break;
                        case "AZURE":
                            client = new AzureAdClient(cfg);
                            break;
                        case "GENERIC":
                        default:
                            client = new OidcClient(cfg);
                            break;
                    }
                    final int count = index.intValue();
                    if (count > 0) {
                        client.setName(client.getClass().getSimpleName() + count);
                    }
                    index.incrementAndGet();
                    LOGGER.debug("Created client [{}]", client);
                    properties.add(client);
                });
    }

    @RefreshScope
    @Bean
    public Clients builtClients() {
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

        LOGGER.debug("The following clients are built: [{}]", clients);
        if (clients.isEmpty()) {
            LOGGER.warn("No delegated authentication clients are defined/configured");
        }

        LOGGER.info("Located and prepared [{}] delegated authentication client(s)", clients.size());
        return new Clients(casProperties.getServer().getLoginUrl(), new ArrayList<>(clients));
    }

    @ConditionalOnMissingBean(name = "clientPrincipalFactory")
    @Bean
    public PrincipalFactory clientPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
        return new ClientAuthenticationMetaDataPopulator();
    }

    @Bean
    public Action saml2ClientLogoutAction() {
        return new SAML2ClientLogoutAction(builtClients());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler clientAuthenticationHandler() {
        final ClientAuthenticationHandler h = new ClientAuthenticationHandler(casProperties.getAuthn().getPac4j().getName(), servicesManager,
                clientPrincipalFactory(), builtClients());
        h.setTypedIdUsed(casProperties.getAuthn().getPac4j().isTypedIdUsed());
        return h;
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (!builtClients().findAllClients().isEmpty()) {
            LOGGER.info("Registering delegated authentication clients...");
            plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler(), personDirectoryPrincipalResolver);
            plan.registerMetadataPopulator(clientAuthenticationMetaDataPopulator());
        }
    }
}
