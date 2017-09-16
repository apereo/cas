package org.apereo.cas.mgmt.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.webapp.mgmt.ManagementWebappProperties;
import org.apereo.cas.mgmt.CasManagementUtils;
import org.apereo.cas.mgmt.authz.CasRoleBasedAuthorizer;
import org.apereo.cas.util.ResourceUtils;
import org.pac4j.cas.client.direct.DirectCasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator;
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.IpClient;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is {@link CasManagementAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casManagementAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasManagementAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasManagementAuthenticationConfiguration.class);

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @ConditionalOnMissingBean(name = "requireAnyRoleAuthorizer")
    @Bean
    @RefreshScope
    public Authorizer requireAnyRoleAuthorizer() {
        return new CasRoleBasedAuthorizer(casProperties.getMgmt().getAdminRoles());
    }

    @ConditionalOnMissingBean(name = "authenticationClients")
    @Bean
    @RefreshScope
    public List<Client> authenticationClients() {
        final List<Client> clients = new ArrayList<>();

        if (StringUtils.hasText(casProperties.getServer().getName())) {
            LOGGER.debug("Configuring an authentication strategy based on CAS running at [{}]", casProperties.getServer().getName());
            final CasConfiguration cfg = new CasConfiguration(casProperties.getServer().getLoginUrl());
            final DirectCasClient client = new DirectCasClient(cfg);
            client.setAuthorizationGenerator(authorizationGenerator());
            client.setName("CasClient");
            clients.add(client);
        } else {
            LOGGER.debug("Skipping CAS authentication strategy configuration; no CAS server name is defined");
        }

        if (StringUtils.hasText(casProperties.getMgmt().getAuthzIpRegex())) {
            LOGGER.info("Configuring an authentication strategy based on authorized IP addresses matching [{}]", casProperties.getMgmt().getAuthzIpRegex());
            final IpClient ipClient = new IpClient(new IpRegexpAuthenticator(casProperties.getMgmt().getAuthzIpRegex()));
            ipClient.setName("IpClient");
            ipClient.setAuthorizationGenerator(getStaticAdminRolesAuthorizationGenerator());
            clients.add(ipClient);
        } else {
            LOGGER.debug("Skipping IP address authentication strategy configuration; no pattern is defined");
        }

        if (clients.isEmpty()) {
            LOGGER.warn("No authentication strategy is defined, CAS will establish an anonymous authentication mode whereby access is immediately granted. "
                    + "This may NOT be relevant for production purposes. Consider configuring alternative authentication strategies for maximum security.");
            final AnonymousClient anon = AnonymousClient.INSTANCE;
            anon.setAuthorizationGenerator(getStaticAdminRolesAuthorizationGenerator());
            clients.add(anon);
        }
        return clients;
    }

    

    @ConditionalOnMissingBean(name = "casManagementSecurityConfiguration")
    @Bean
    @RefreshScope
    public Config casManagementSecurityConfiguration() {
        final Config cfg = new Config(CasManagementUtils.getDefaultCallbackUrl(casProperties, serverProperties), authenticationClients());
        cfg.setAuthorizer(requireAnyRoleAuthorizer());
        return cfg;
    }

    @RefreshScope
    @Bean
    public Properties userProperties() {
        try {
            final Properties p = new Properties();

            final ManagementWebappProperties mgmt = casProperties.getMgmt();
            if (ResourceUtils.doesResourceExist(mgmt.getUserPropertiesFile())) {
                p.load(mgmt.getUserPropertiesFile().getInputStream());
            } else {
                LOGGER.warn("Could not locate [{}]", mgmt.getUserPropertiesFile());
            }

            return p;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @ConditionalOnMissingBean(name = "authorizationGenerator")
    @Bean
    @RefreshScope
    public AuthorizationGenerator authorizationGenerator() {
        final List<String> authzAttributes = casProperties.getMgmt().getAuthzAttributes();
        if (!authzAttributes.isEmpty()) {
            if (authzAttributes.stream().anyMatch(a -> a.equals("*"))) {
                return getStaticAdminRolesAuthorizationGenerator();
            }
            return new FromAttributesAuthorizationGenerator(authzAttributes.toArray(new String[]{}), new String[]{});
        }
        return new SpringSecurityPropertiesAuthorizationGenerator(userProperties());
    }

    private AuthorizationGenerator getStaticAdminRolesAuthorizationGenerator() {
        return (context, profile) -> {
            profile.addRoles(casProperties.getMgmt().getAdminRoles());
            return profile;
        };
    }
}
