package org.apereo.cas.support.wsfederation.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.flow.WsFederationAction;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("wsFederationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFederationAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired(required = false)
    @Qualifier("wsfedAttributeMutator")
    private WsFederationAttributeMutator attributeMutator;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Bean
    @RefreshScope
    public WsFederationConfiguration wsFedConfig() {
        final WsFederationConfiguration config = new WsFederationConfiguration();
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfed();
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
    public WsFederationHelper wsFederationHelper() {
        final WsFederationHelper h = new WsFederationHelper();
        h.setConfigBean(this.configBean);
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler adfsAuthNHandler() {
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfed();
        final WsFederationAuthenticationHandler h = new WsFederationAuthenticationHandler();
        h.setPrincipalFactory(adfsPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setName(wsfed.getName());
        return h;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver adfsPrincipalResolver() {
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfed();
        final WsFederationCredentialsToPrincipalResolver r = new WsFederationCredentialsToPrincipalResolver();
        r.setConfiguration(wsFedConfig());
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(wsfed.getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(wsfed.getPrincipal().isReturnNull());
        r.setPrincipalFactory(adfsPrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory adfsPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public Action wsFederationAction() {
        return new WsFederationAction(authenticationSystemSupport, centralAuthenticationService, wsFedConfig(), wsFederationHelper(), servicesManager);
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        final WsFederationProperties wsfed = casProperties.getAuthn().getWsfed();
        if (StringUtils.isNotBlank(wsfed.getIdentityProviderUrl())
                && StringUtils.isNotBlank(wsfed.getIdentityProviderIdentifier())) {

            if (!wsfed.isAttributeResolverEnabled()) {
                authenticationHandlersResolvers.put(adfsAuthNHandler(), null);
            } else {
                authenticationHandlersResolvers.put(adfsAuthNHandler(), adfsPrincipalResolver());
            }
        }
    }
}
