package org.apereo.cas.config.support.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YubiKeyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("yubikeyAuthenticationEventExecutionPlanConfiguration")
public class YubiKeyAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry registry;
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;
    
    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute, 
                yubikeyAuthenticationHandler(),
                yubikeyAuthenticationProvider());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getYubikey().getBypass());
    }

    @ConditionalOnMissingBean(name = "yubikeyPrincipalFactory")
    @Bean
    public PrincipalFactory yubikeyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    
    @Bean
    @RefreshScope
    public YubiKeyAuthenticationHandler yubikeyAuthenticationHandler() {
        final MultifactorAuthenticationProperties.YubiKey yubi = this.casProperties.getAuthn().getMfa().getYubikey();

        if (StringUtils.isBlank(yubi.getSecretKey())) {
            throw new IllegalArgumentException("Yubikey secret key cannot be blank");
        }
        if (yubi.getClientId() <= 0) {
            throw new IllegalArgumentException("Yubikey client id is undefined");
        }
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(yubi.getName(), servicesManager, yubikeyPrincipalFactory(),
                yubi.getClientId(), yubi.getSecretKey(), this.registry);

        if (!casProperties.getAuthn().getMfa().getYubikey().getApiUrls().isEmpty()) {
            final String[] urls = casProperties.getAuthn().getMfa().getYubikey().getApiUrls().toArray(new String[]{});
            handler.getClient().setWsapiUrls(urls);
        }
        return handler;
    }
    
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        final YubiKeyMultifactorAuthenticationProvider p = new YubiKeyMultifactorAuthenticationProvider(
                yubikeyAuthenticationHandler(), 
                this.httpClient);
        p.setBypassEvaluator(yubikeyBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getYubikey().getRank());
        p.setId(casProperties.getAuthn().getMfa().getYubikey().getId());
        return p;
    }
        
    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        final MultifactorAuthenticationProperties.YubiKey yubi = casProperties.getAuthn().getMfa().getYubikey();
        if (yubi.getClientId() > 0 && StringUtils.isNotBlank(yubi.getSecretKey())) {
            plan.registerAuthenticationHandler(yubikeyAuthenticationHandler());
            plan.registerMetadataPopulator(yubikeyAuthenticationMetaDataPopulator());
        }
    }
}
