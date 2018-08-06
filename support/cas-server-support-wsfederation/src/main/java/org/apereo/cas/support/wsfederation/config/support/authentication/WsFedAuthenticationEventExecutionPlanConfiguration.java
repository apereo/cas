package org.apereo.cas.support.wsfederation.config.support.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegatedCookieProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieCipherExecutor;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieGenerator;
import org.apereo.cas.web.support.DefaultCasCookieValueManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link WsFedAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("wsfedAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class WsFedAuthenticationEventExecutionPlanConfiguration {

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

    private WsFederationConfiguration getWsFederationConfiguration(final WsFederationDelegationProperties wsfed) {
        final WsFederationConfiguration config = new WsFederationConfiguration();
        config.setAttributesType(WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.valueOf(wsfed.getAttributesType()));
        config.setIdentityAttribute(wsfed.getIdentityAttribute());
        config.setIdentityProviderIdentifier(wsfed.getIdentityProviderIdentifier());
        config.setIdentityProviderUrl(wsfed.getIdentityProviderUrl());
        config.setTolerance(Beans.newDuration(wsfed.getTolerance()).toMillis());
        config.setRelyingPartyIdentifier(wsfed.getRelyingPartyIdentifier());

        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getSigningCertificateResources())
            .forEach(s -> config.getSigningCertificateResources().add(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionPrivateKey())
            .forEach(s -> config.setEncryptionPrivateKey(this.resourceLoader.getResource(s)));

        org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionCertificate())
            .forEach(s -> config.setEncryptionCertificate(this.resourceLoader.getResource(s)));

        config.setEncryptionPrivateKeyPassword(wsfed.getEncryptionPrivateKeyPassword());
        config.setAttributeMutator(this.attributeMutator);
        config.setAutoRedirect(wsfed.isAutoRedirect());
        config.setName(wsfed.getName());


        final WsFederationDelegatedCookieProperties cookie = wsfed.getCookie();
        final EncryptionJwtSigningJwtCryptographyProperties crypto = cookie.getCrypto();
        final CipherExecutor cipher;
        if (crypto.isEnabled()) {
            cipher = new WsFederationCookieCipherExecutor(crypto.getEncryption().getKey(), crypto.getSigning().getKey(), crypto.getAlg());
        } else {
            LOGGER.info("WsFederation delegated authentication cookie encryption/signing is turned off and "
                + "MAY NOT be safe in a production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "delegated authentication cookie.");
            cipher = CipherExecutor.noOp();
        }
        final WsFederationCookieGenerator cookieGen = new WsFederationCookieGenerator(new DefaultCasCookieValueManager(cipher, cookie),
            cookie.getName(), cookie.getPath(), cookie.getMaxAge(),
            cookie.isSecure(), cookie.getDomain(), cookie.isHttpOnly());
        config.setCookieGenerator(cookieGen);

        config.initialize();
        return config;
    }

    @ConditionalOnMissingBean(name = "wsFederationConfigurations")
    @Bean
    @RefreshScope
    public Collection<WsFederationConfiguration> wsFederationConfigurations() {
        final Collection<WsFederationConfiguration> col = new HashSet<>();
        casProperties.getAuthn().getWsfed().forEach(wsfed -> {
            final WsFederationConfiguration cfg = getWsFederationConfiguration(wsfed);
            col.add(cfg);
        });
        return col;
    }
    
    @ConditionalOnMissingBean(name = "adfsPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory adfsPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "wsfedAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer wsfedAuthenticationEventExecutionPlanConfigurer() {
        return plan -> casProperties.getAuthn().getWsfed()
            .stream()
            .filter(wsfed -> StringUtils.isNotBlank(wsfed.getIdentityProviderUrl())
                && StringUtils.isNotBlank(wsfed.getIdentityProviderIdentifier()))
            .forEach(wsfed -> {
                final AuthenticationHandler handler =
                    new WsFederationAuthenticationHandler(wsfed.getName(), servicesManager, adfsPrincipalFactory());
                if (!wsfed.isAttributeResolverEnabled()) {
                    plan.registerAuthenticationHandler(handler);
                } else {
                    final Collection<WsFederationConfiguration> configurations = wsFederationConfigurations();
                    final WsFederationConfiguration cfg = configurations.stream()
                        .filter(c -> c.getIdentityProviderUrl().equals(wsfed.getIdentityProviderUrl()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Unable to find configuration for identity provider " + wsfed.getIdentityProviderUrl()));

                    final WsFederationCredentialsToPrincipalResolver r =
                        new WsFederationCredentialsToPrincipalResolver(attributeRepository, adfsPrincipalFactory(),
                            wsfed.getPrincipal().isReturnNull(),
                            wsfed.getPrincipal().getPrincipalAttribute(),
                            cfg);
                    plan.registerAuthenticationHandlerWithPrincipalResolver(handler, r);
                }
            });
    }
}
