package org.apereo.cas.support.wsfederation.config.support.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * This is {@link WsFedAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("wsfedAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFedAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired(required = false)
    @Qualifier("wsfedAttributeMutator")
    private WsFederationAttributeMutator attributeMutator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public AuthenticationHandler adfsAuthNHandler() {
        final WsFederationDelegationProperties wsfed = casProperties.getAuthn().getWsfed();
        return new WsFederationAuthenticationHandler(wsfed.getName(), servicesManager, adfsPrincipalFactory());
    }

    @Bean
    @RefreshScope
    public WsFederationConfiguration wsFedConfig() {
        final WsFederationConfiguration config = new WsFederationConfiguration();
        final WsFederationDelegationProperties wsfed = casProperties.getAuthn().getWsfed();
        config.setAttributesType(WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.valueOf(wsfed.getAttributesType()));
        config.setIdentityAttribute(wsfed.getIdentityAttribute());
        config.setIdentityProviderIdentifier(wsfed.getIdentityProviderIdentifier());
        config.setIdentityProviderUrl(wsfed.getIdentityProviderUrl());
        config.setTolerance(wsfed.getTolerance());
        config.setRelyingPartyIdentifier(wsfed.getRelyingPartyIdentifier());
        
        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getSigningCertificateResources())
                .forEach(s -> config.getSigningCertificateResources().add(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionPrivateKey())
                .forEach(s -> config.setEncryptionPrivateKey(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionCertificate())
                .forEach(s -> config.setEncryptionCertificate(this.resourceLoader.getResource(s)));

        config.setEncryptionPrivateKeyPassword(wsfed.getEncryptionPrivateKeyPassword());
        config.setAttributeMutator(this.attributeMutator);
        return config;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver adfsPrincipalResolver() {
        final WsFederationDelegationProperties wsfed = casProperties.getAuthn().getWsfed();
        final WsFederationCredentialsToPrincipalResolver r = new WsFederationCredentialsToPrincipalResolver();
        r.setConfiguration(wsFedConfig());
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(wsfed.getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(wsfed.getPrincipal().isReturnNull());
        r.setPrincipalFactory(adfsPrincipalFactory());
        return r;
    }

    @ConditionalOnMissingBean(name = "adfsPrincipalFactory")
    @Bean
    public PrincipalFactory adfsPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        final WsFederationDelegationProperties wsfed = casProperties.getAuthn().getWsfed();
        if (StringUtils.isNotBlank(wsfed.getIdentityProviderUrl()) && StringUtils.isNotBlank(wsfed.getIdentityProviderIdentifier())) {

            if (!wsfed.isAttributeResolverEnabled()) {
                plan.registerAuthenticationHandler(adfsAuthNHandler());
            } else {
                plan.registerAuthenticationHandlerWithPrincipalResolver(adfsAuthNHandler(), adfsPrincipalResolver());
            }
        }
    }
}
