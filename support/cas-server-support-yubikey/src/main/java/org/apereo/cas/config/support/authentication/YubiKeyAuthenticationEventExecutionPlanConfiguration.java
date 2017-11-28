package org.apereo.cas.config.support.authentication;

import com.yubico.client.v2.YubicoClient;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.DefaultYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.registry.JsonYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.WhitelistYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountSaveRegistrationAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.YubiKeyMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link YubiKeyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("yubikeyAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YubiKeyAuthenticationEventExecutionPlanConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(YubiKeyAuthenticationEventExecutionPlanConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

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
        return MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getYubikey().getBypass());
    }

    @ConditionalOnMissingBean(name = "yubikeyPrincipalFactory")
    @Bean
    public PrincipalFactory yubikeyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "yubicoClient")
    public YubicoClient yubicoClient() {
        final YubiKeyMultifactorProperties yubi = this.casProperties.getAuthn().getMfa().getYubikey();

        if (StringUtils.isBlank(yubi.getSecretKey())) {
            throw new IllegalArgumentException("Yubikey secret key cannot be blank");
        }
        if (yubi.getClientId() <= 0) {
            throw new IllegalArgumentException("Yubikey client id is undefined");
        }

        final YubicoClient client = YubicoClient.getClient(yubi.getClientId(), yubi.getSecretKey());
        if (!yubi.getApiUrls().isEmpty()) {
            final String[] urls = yubi.getApiUrls().toArray(new String[]{});
            client.setWsapiUrls(urls);
        }
        return client;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationHandler")
    public AuthenticationHandler yubikeyAuthenticationHandler() {
        final YubiKeyMultifactorProperties yubi = this.casProperties.getAuthn().getMfa().getYubikey();
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(yubi.getName(),
                servicesManager, yubikeyPrincipalFactory(),
                yubicoClient(), yubiKeyAccountRegistry());
        return handler;
    }

    @Bean
    @RefreshScope
    public Action yubiKeyAccountRegistrationAction() {
        return new YubiKeyAccountCheckRegistrationAction(yubiKeyAccountRegistry());
    }

    @Bean
    @RefreshScope
    public Action yubiKeySaveAccountRegistrationAction() {
        return new YubiKeyAccountSaveRegistrationAction(yubiKeyAccountRegistry());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubiKeyAccountValidator")
    public YubiKeyAccountValidator yubiKeyAccountValidator() {
        return new DefaultYubiKeyAccountValidator(yubicoClient());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubiKeyAccountRegistry")
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        final YubiKeyMultifactorProperties yubi = casProperties.getAuthn().getMfa().getYubikey();

        if (yubi.getJsonFile() != null) {
            LOGGER.debug("Using JSON resource [{}] as the YubiKey account registry", yubi.getJsonFile());
            return new JsonYubiKeyAccountRegistry(yubi.getJsonFile(), yubiKeyAccountValidator());
        }
        if (yubi.getAllowedDevices() != null) {
            LOGGER.debug("Using statically-defined devices for [{}] as the YubiKey account registry",
                    yubi.getAllowedDevices().keySet());
            return new WhitelistYubiKeyAccountRegistry(yubi.getAllowedDevices(), yubiKeyAccountValidator());
        }

        LOGGER.warn("All credentials are considered eligible for YubiKey authentication. "
                        + "Consider providing an account registry implementation via [{}]",
                YubiKeyAccountRegistry.class.getName());
        return new OpenYubiKeyAccountRegistry();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        final YubiKeyMultifactorAuthenticationProvider p = new YubiKeyMultifactorAuthenticationProvider(
                yubicoClient(),
                this.httpClient);
        p.setBypassEvaluator(yubikeyBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getYubikey().getRank());
        p.setId(casProperties.getAuthn().getMfa().getYubikey().getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "yubikeyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer yubikeyAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            final YubiKeyMultifactorProperties yubi = casProperties.getAuthn().getMfa().getYubikey();
            if (yubi.getClientId() > 0 && StringUtils.isNotBlank(yubi.getSecretKey())) {
                plan.registerAuthenticationHandler(yubikeyAuthenticationHandler());
                plan.registerMetadataPopulator(yubikeyAuthenticationMetaDataPopulator());
            }
        };
    }
}
