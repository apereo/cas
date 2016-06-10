package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.WsFedApplicationContextWrapper;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.flow.WsFederationAction;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("wsFederationConfiguration")
public class WsFederationAuthenticationConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adfsAuthNHandler")
    private AuthenticationHandler adfsAuthNHandler;

    @Autowired
    @Qualifier("adfsPrincipalResolver")
    private PrincipalResolver adfsPrincipalResolver;

    @Bean
    public BaseApplicationContextWrapper wsFedApplicationContextWrapper() {
        return new WsFedApplicationContextWrapper(this.adfsAuthNHandler, this.adfsPrincipalResolver,
                casProperties.getWsfed().isAttributeResolverEnabled());
    }

    @Bean
    @RefreshScope
    public WsFederationConfiguration wsFedConfig() {
        final WsFederationConfiguration config = new WsFederationConfiguration();

        config.setAttributesType(WsFederationConfiguration.WsFedPrincipalResolutionAttributesType
                .valueOf(casProperties.getWsfed().getAttributesType()));
        config.setIdentityAttribute(casProperties.getWsfed().getIdentityAttribute());
        config.setIdentityProviderIdentifier(casProperties.getWsfed().getIdentityProviderIdentifier());
        config.setIdentityProviderUrl(casProperties.getWsfed().getIdentityProviderUrl());
        config.setTolerance(casProperties.getWsfed().getTolerance());
        config.setRelyingPartyIdentifier(casProperties.getWsfed().getRelyingPartyIdentifier());
        StringUtils.commaDelimitedListToSet(casProperties.getWsfed().getSigningCertificateResources())
                .forEach(s -> config.getSigningCertificateResources().add(this.resourceLoader.getResource(s)));

        return config;
    }

    @Bean
    @RefreshScope
    public WsFederationHelper wsFederationHelper() {
        return new WsFederationHelper();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler adfsAuthNHandler() {
        return new WsFederationAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver adfsPrincipalResolver() {
        return new WsFederationCredentialsToPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public Action wsFederationAction() {
        return new WsFederationAction();
    }
}
