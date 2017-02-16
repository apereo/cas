package org.apereo.cas.support.pac4j.config.support.authentication;

import com.nimbusds.jose.JWSAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
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
import org.pac4j.oauth.client.DropBoxClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.FoursquareClient;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.LinkedIn2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.client.WindowsLiveClient;
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
        final GitHubClient client = new GitHubClient(github.getId(), github.getSecret());
        properties.add(client);
    }

    private void configureDropboxClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Dropbox db = casProperties.getAuthn().getPac4j().getDropbox();
        final DropBoxClient client = new DropBoxClient(db.getId(), db.getSecret());
        properties.add(client);
    }

    private void configureWindowsLiveClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.WindowsLive live = casProperties.getAuthn().getPac4j().getWindowsLive();
        final WindowsLiveClient client = new WindowsLiveClient(live.getId(), live.getSecret());
        properties.add(client);
    }

    private void configureYahooClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Yahoo yahoo = casProperties.getAuthn().getPac4j().getYahoo();
        final YahooClient client = new YahooClient(yahoo.getId(), yahoo.getSecret());
        properties.add(client);
    }

    private void configureFoursquareClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Foursquare foursquare = casProperties.getAuthn().getPac4j().getFoursquare();
        final FoursquareClient client = new FoursquareClient(foursquare.getId(), foursquare.getSecret());
        properties.add(client);
    }

    private void configureGoogleClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Google google = casProperties.getAuthn().getPac4j().getGoogle();
        final Google2Client client = new Google2Client(google.getId(), google.getSecret());

        if (StringUtils.isNotBlank(google.getScope())) {
            client.setScope(Google2Client.Google2Scope.valueOf(google.getScope().toUpperCase()));
        }
        properties.add(client);
    }

    private void configureFacebookClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Facebook fb = casProperties.getAuthn().getPac4j().getFacebook();
        final FacebookClient client = new FacebookClient(fb.getId(), fb.getSecret());
        client.setScope(fb.getScope());
        client.setFields(fb.getFields());
        properties.add(client);
    }

    private void configureLinkedInClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.LinkedIn fb = casProperties.getAuthn().getPac4j().getLinkedIn();
        final LinkedIn2Client client = new LinkedIn2Client(fb.getId(), fb.getSecret());

        if (StringUtils.isNotBlank(fb.getScope())) {
            client.setScope(fb.getScope());
        }

        if (StringUtils.isNotBlank(fb.getFields())) {
            client.setFields(fb.getFields());
        }
        properties.add(client);
    }

    private void configureTwitterClient(final Collection<BaseClient> properties) {
        final Pac4jProperties.Twitter twitter = casProperties.getAuthn().getPac4j().getTwitter();
        final TwitterClient client = new TwitterClient(twitter.getId(), twitter.getSecret());
        properties.add(client);
    }

    private void configureCasClient(final Collection<BaseClient> properties) {
        casProperties.getAuthn().getPac4j().getCas().forEach(cas -> {
            final CasConfiguration cfg = new CasConfiguration(cas.getLoginUrl(), cas.getProtocol());
            final CasClient client = new CasClient(cfg);
            properties.add(client);
        });
    }

    private void configureSamlClient(final Collection<BaseClient> properties) {
        casProperties.getAuthn().getPac4j().getSaml().forEach(saml -> {
            final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration(saml.getKeystorePath(), saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), saml.getIdentityProviderMetadataPath());
            cfg.setMaximumAuthenticationLifetime(saml.getMaximumAuthenticationLifetime());
            cfg.setServiceProviderEntityId(saml.getServiceProviderEntityId());
            cfg.setServiceProviderMetadataPath(saml.getServiceProviderMetadataPath());
            cfg.setDestinationBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
            final SAML2Client client = new SAML2Client(cfg);
            properties.add(client);
        });
    }

    private void configureOidcClient(final Collection<BaseClient> properties) {
        casProperties.getAuthn().getPac4j().getOidc().forEach(oidc -> {

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
            properties.add(client);
        });
    }

    @RefreshScope
    @Bean
    public Clients builtClients() {
        // turn the properties file into a map of properties
        final Set<BaseClient> clients = new LinkedHashSet<>();

        configureCasClient(clients);
        configureFacebookClient(clients);
        configureOidcClient(clients);
        configureSamlClient(clients);
        configureTwitterClient(clients);
        configureDropboxClient(clients);
        configureFoursquareClient(clients);
        configureGithubClient(clients);
        configureGoogleClient(clients);
        configureWindowsLiveClient(clients);
        configureYahooClient(clients);
        configureLinkedInClient(clients);

        if (clients.isEmpty()) {
            throw new IllegalArgumentException("At least one client must be defined");
        }

        LOGGER.debug("The following clients are built: [{}]", clients);
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
        final ClientAuthenticationHandler h = new ClientAuthenticationHandler(builtClients());
        h.setPrincipalFactory(clientPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setTypedIdUsed(casProperties.getAuthn().getPac4j().isTypedIdUsed());
        h.setName(casProperties.getAuthn().getPac4j().getName());
        return h;
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler(), personDirectoryPrincipalResolver);
        plan.registerMetadataPopulator(clientAuthenticationMetaDataPopulator());
    }
}
