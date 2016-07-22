package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
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

import org.apache.commons.lang3.StringUtils;

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
    private AuthenticationSystemSupport authenticationSystemSupport =
            new DefaultAuthenticationSystemSupport();


    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Bean
    @RefreshScope
    public WsFederationConfiguration wsFedConfig() {
        final WsFederationConfiguration config = new WsFederationConfiguration();

        config.setAttributesType(WsFederationConfiguration.WsFedPrincipalResolutionAttributesType
                .valueOf(casProperties.getAuthn().getWsfed().getAttributesType()));
        config.setIdentityAttribute(casProperties.getAuthn().getWsfed().getIdentityAttribute());
        config.setIdentityProviderIdentifier(casProperties.getAuthn().getWsfed().getIdentityProviderIdentifier());
        config.setIdentityProviderUrl(casProperties.getAuthn().getWsfed().getIdentityProviderUrl());
        config.setTolerance(casProperties.getAuthn().getWsfed().getTolerance());
        config.setRelyingPartyIdentifier(casProperties.getAuthn().getWsfed().getRelyingPartyIdentifier());
        org.springframework.util.StringUtils.commaDelimitedListToSet(casProperties.getAuthn().getWsfed().getSigningCertificateResources())
                .forEach(s -> config.getSigningCertificateResources().add(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(casProperties.getAuthn().getWsfed().getEncryptionPrivateKey())
                .forEach(s -> config.setEncryptionPrivateKey(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(casProperties.getAuthn().getWsfed().getEncryptionCertificate())
                .forEach(s -> config.setEncryptionCertificate(this.resourceLoader.getResource(s)));

        config.setEncryptionPrivateKeyPassword(casProperties.getAuthn().getWsfed().getEncryptionPrivateKeyPassword());
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
        final WsFederationAuthenticationHandler h =
                new WsFederationAuthenticationHandler();
        h.setPrincipalFactory(adfsPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver adfsPrincipalResolver() {
        final WsFederationCredentialsToPrincipalResolver r =
                new WsFederationCredentialsToPrincipalResolver();
        r.setConfiguration(wsFedConfig());
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getWsfed().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getWsfed().getPrincipal().isReturnNull());
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
        final WsFederationAction a = new WsFederationAction();

        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setConfiguration(wsFedConfig());
        a.setWsFederationHelper(wsFederationHelper());
        a.setServicesManager(this.servicesManager);

        return a;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {

        if (StringUtils.isNotBlank(casProperties.getAuthn().getWsfed().getIdentityProviderUrl()) 
           && StringUtils.isNotBlank(casProperties.getAuthn().getWsfed().getIdentityProviderIdentifier())) {

            if (!casProperties.getAuthn().getWsfed().isAttributeResolverEnabled()) {
                authenticationHandlersResolvers.put(adfsAuthNHandler(), null);
            } else {
                authenticationHandlersResolvers.put(adfsAuthNHandler(), adfsPrincipalResolver());
            }
        }
    }
}
