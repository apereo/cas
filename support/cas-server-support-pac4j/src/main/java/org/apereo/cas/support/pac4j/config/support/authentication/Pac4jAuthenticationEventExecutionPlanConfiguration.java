package org.apereo.cas.support.pac4j.config.support.authentication;

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
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void configureGithubClient(final Map<String, String> properties) {
        final Pac4jProperties.Github github = casProperties.getAuthn().getPac4j().getGithub();
        properties.put(PropertiesConfigFactory.GITHUB_ID, github.getId());
        properties.put(PropertiesConfigFactory.GITHUB_SECRET, github.getSecret());
    }

    private void configureDropboxClient(final Map<String, String> properties) {
        final Pac4jProperties.Dropbox db = casProperties.getAuthn().getPac4j().getDropbox();
        properties.put(PropertiesConfigFactory.DROPBOX_ID, db.getId());
        properties.put(PropertiesConfigFactory.DROPBOX_SECRET, db.getSecret());
    }

    private void configureWindowsLiveClient(final Map<String, String> properties) {
        final Pac4jProperties.WindowsLive live = casProperties.getAuthn().getPac4j().getWindowsLive();
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_ID, live.getId());
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_SECRET, live.getSecret());
    }

    private void configureYahooClient(final Map<String, String> properties) {
        final Pac4jProperties.Yahoo yahoo = casProperties.getAuthn().getPac4j().getYahoo();
        properties.put(PropertiesConfigFactory.YAHOO_ID, yahoo.getId());
        properties.put(PropertiesConfigFactory.YAHOO_SECRET, yahoo.getSecret());
    }

    private void configureFoursquareClient(final Map<String, String> properties) {
        final Pac4jProperties.Foursquare foursquare = casProperties.getAuthn().getPac4j().getFoursquare();
        properties.put(PropertiesConfigFactory.FOURSQUARE_ID, foursquare.getId());
        properties.put(PropertiesConfigFactory.FOURSQUARE_SECRET, foursquare.getSecret());
    }

    private void configureGoogleClient(final Map<String, String> properties) {
        final Pac4jProperties.Google google = casProperties.getAuthn().getPac4j().getGoogle();
        properties.put(PropertiesConfigFactory.GOOGLE_ID, google.getId());
        properties.put(PropertiesConfigFactory.GOOGLE_SECRET, google.getSecret());
        properties.put(PropertiesConfigFactory.GOOGLE_SCOPE, google.getScope());
    }

    private void configureFacebookClient(final Map<String, String> properties) {
        final Pac4jProperties.Facebook fb = casProperties.getAuthn().getPac4j().getFacebook();
        properties.put(PropertiesConfigFactory.FACEBOOK_ID, fb.getId());
        properties.put(PropertiesConfigFactory.FACEBOOK_SECRET, fb.getSecret());
        properties.put(PropertiesConfigFactory.FACEBOOK_SCOPE, fb.getScope());
        properties.put(PropertiesConfigFactory.FACEBOOK_FIELDS, fb.getFields());
    }

    private void configureLinkedInClient(final Map<String, String> properties) {
        final Pac4jProperties.LinkedIn fb = casProperties.getAuthn().getPac4j().getLinkedIn();
        properties.put(PropertiesConfigFactory.LINKEDIN_ID, fb.getId());
        properties.put(PropertiesConfigFactory.LINKEDIN_SECRET, fb.getSecret());
        properties.put(PropertiesConfigFactory.LINKEDIN_SCOPE, fb.getScope());
        properties.put(PropertiesConfigFactory.LINKEDIN_FIELDS, fb.getFields());
    }

    private void configureTwitterClient(final Map<String, String> properties) {
        final Pac4jProperties.Twitter twitter = casProperties.getAuthn().getPac4j().getTwitter();
        properties.put(PropertiesConfigFactory.TWITTER_ID, twitter.getId());
        properties.put(PropertiesConfigFactory.TWITTER_SECRET, twitter.getSecret());
    }

    private void configureCasClient(final Map<String, String> properties) {
        for (Integer i = 0; i < casProperties.getAuthn().getPac4j().getCas().size(); i++) {
            final Pac4jProperties.Cas cas = casProperties.getAuthn().getPac4j().getCas().get(0);
            properties.put(getConfigurationKey(PropertiesConfigFactory.CAS_LOGIN_URL, i), cas.getLoginUrl());
            properties.put(getConfigurationKey(PropertiesConfigFactory.CAS_PROTOCOL, i), cas.getProtocol());
        }
    }

    private void configureSamlClient(final Map<String, String> properties) {
        for (Integer i = 0; i < casProperties.getAuthn().getPac4j().getSaml().size(); i++) {
            final Pac4jProperties.Saml saml = casProperties.getAuthn().getPac4j().getSaml().get(i);

            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_IDENTITY_PROVIDER_METADATA_PATH, i), saml.getIdentityProviderMetadataPath());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_KEYSTORE_PASSWORD, i), saml.getKeystorePassword());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_KEYSTORE_PATH, i), saml.getKeystorePath());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_MAXIMUM_AUTHENTICATION_LIFETIME, i), saml.getMaximumAuthenticationLifetime());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_PRIVATE_KEY_PASSWORD, i), saml.getPrivateKeyPassword());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_ENTITY_ID, i), saml.getServiceProviderEntityId());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_METADATA_PATH, i), saml.getServiceProviderMetadataPath());
            properties.put(getConfigurationKey(PropertiesConfigFactory.SAML_DESTINATION_BINDING_TYPE, i), SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        }
    }

    private void configureOidcClient(final Map<String, String> properties) {
        for (Integer i = 0; i < casProperties.getAuthn().getPac4j().getOidc().size(); i++) {
            final Pac4jProperties.Oidc oidc = casProperties.getAuthn().getPac4j().getOidc().get(i);
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY1, i), oidc.getCustomParamKey1());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY2, i), oidc.getCustomParamKey2());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE1, i), oidc.getCustomParamValue1());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE2, i), oidc.getCustomParamValue2());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_DISCOVERY_URI, i), oidc.getDiscoveryUri());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_ID, i), oidc.getId());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_MAX_CLOCK_SKEW, i), oidc.getMaxClockSkew());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_PREFERRED_JWS_ALGORITHM, i), oidc.getPreferredJwsAlgorithm());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_SECRET, i), oidc.getSecret());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_USE_NONCE, i), oidc.getUseNonce());
            properties.put(getConfigurationKey(PropertiesConfigFactory.OIDC_SCOPE, i), oidc.getScope());
        }
    }

    private String getConfigurationKey(final String key, final int index) {
        if (index == 0) {
            return key;
        }
        return key.concat(".").concat(String.valueOf(index));
    }

    @RefreshScope
    @Bean
    public Clients builtClients() {
        final List<Client> allClients = new ArrayList<>();

        // turn the properties file into a map of properties
        final Map<String, String> properties = new HashMap<>();

        configureCasClient(properties);
        configureFacebookClient(properties);
        configureOidcClient(properties);
        configureSamlClient(properties);
        configureTwitterClient(properties);
        configureDropboxClient(properties);
        configureFoursquareClient(properties);
        configureGithubClient(properties);
        configureGoogleClient(properties);
        configureWindowsLiveClient(properties);
        configureYahooClient(properties);
        configureLinkedInClient(properties);

        // add the new clients found via properties first
        final ConfigFactory configFactory = new PropertiesConfigFactory(properties);
        final Config propertiesConfig = configFactory.build();
        allClients.addAll(propertiesConfig.getClients().getClients());

        if (allClients.isEmpty()) {
            throw new IllegalArgumentException("At least one client must be defined");
        }

        LOGGER.debug("The following clients are built: [{}]", allClients);
        return new Clients(casProperties.getServer().getLoginUrl(), allClients);
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
