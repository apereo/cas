package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.DefaultYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyCredential;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.JsonYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.PermissiveYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.RestfulYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.YubiKeyAccountRegistryEndpoint;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;

import com.yubico.client.v2.YubicoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("yubikeyAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class YubiKeyAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private ObjectProvider<CipherExecutor> yubikeyAccountCipherExecutor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("yubikeyBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> yubikeyBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationMetaDataPopulator")
    public AuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
            authenticationContextAttribute,
            yubikeyAuthenticationHandler(),
            yubikeyMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "yubikeyPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory yubikeyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "yubicoClient")
    public YubicoClient yubicoClient() {
        val yubi = this.casProperties.getAuthn().getMfa().getYubikey();

        if (StringUtils.isBlank(yubi.getSecretKey())) {
            throw new IllegalArgumentException("Yubikey secret key cannot be blank");
        }
        if (yubi.getClientId() <= 0) {
            throw new IllegalArgumentException("Yubikey client id is undefined");
        }

        val client = YubicoClient.getClient(yubi.getClientId(), yubi.getSecretKey());
        if (!yubi.getApiUrls().isEmpty()) {
            val urls = yubi.getApiUrls().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
            client.setWsapiUrls(urls);
        }
        return client;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationHandler")
    public AuthenticationHandler yubikeyAuthenticationHandler() {
        val yubi = this.casProperties.getAuthn().getMfa().getYubikey();
        return new YubiKeyAuthenticationHandler(yubi.getName(),
            servicesManager.getObject(), yubikeyPrincipalFactory(),
            yubicoClient(), yubiKeyAccountRegistry(),
            yubi.getOrder());
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
        val yubi = casProperties.getAuthn().getMfa().getYubikey();

        val cipher = yubikeyAccountCipherExecutor.getObject();
        if (yubi.getJsonFile() != null) {
            LOGGER.debug("Using JSON resource [{}] as the YubiKey account registry", yubi.getJsonFile());
            val registry = new JsonYubiKeyAccountRegistry(yubi.getJsonFile(), yubiKeyAccountValidator());
            registry.setCipherExecutor(cipher);
            return registry;
        }
        if (StringUtils.isNotBlank(yubi.getRest().getUrl())) {
            LOGGER.debug("Using REST API resource [{}] as the YubiKey account registry", yubi.getRest().getUrl());
            val registry = new RestfulYubiKeyAccountRegistry(yubi.getRest(), yubiKeyAccountValidator());
            registry.setCipherExecutor(cipher);
            return registry;
        }
        if (yubi.getAllowedDevices() != null && !yubi.getAllowedDevices().isEmpty()) {
            LOGGER.debug("Using statically-defined devices for [{}] as the YubiKey account registry",
                yubi.getAllowedDevices().keySet());
            val map = (Map<String, YubiKeyAccount>) yubi.getAllowedDevices().entrySet()
                .stream()
                .map(entry -> YubiKeyAccount.builder()
                    .id(System.currentTimeMillis())
                    .username(entry.getKey())
                    .devices(List.of(YubiKeyRegisteredDevice.builder()
                        .publicId(entry.getValue())
                        .name(UUID.randomUUID().toString())
                        .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                        .build()))
                    .build())
                .collect(Collectors.toMap(YubiKeyAccount::getUsername, acct -> acct));
            val registry = new PermissiveYubiKeyAccountRegistry(map, yubiKeyAccountValidator());
            registry.setCipherExecutor(CipherExecutor.noOpOfSerializableToString());
            return registry;
        }

        LOGGER.warn("All credentials are considered eligible for YubiKey authentication. "
                + "Consider providing an account registry implementation via [{}]",
            YubiKeyAccountRegistry.class.getName());
        val registry = new OpenYubiKeyAccountRegistry(new DefaultYubiKeyAccountValidator(yubicoClient()));
        registry.setCipherExecutor(cipher);
        return registry;
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public YubiKeyAccountRegistryEndpoint yubiKeyAccountRegistryEndpoint() {
        return new YubiKeyAccountRegistryEndpoint(casProperties, yubiKeyAccountRegistry());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyMultifactorAuthenticationProvider() {
        val yubi = casProperties.getAuthn().getMfa().getYubikey();
        val p = new YubiKeyMultifactorAuthenticationProvider(yubicoClient(), httpClient.getObject());
        p.setBypassEvaluator(yubikeyBypassEvaluator.getObject());
        p.setFailureMode(yubi.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getObject());
        p.setOrder(yubi.getRank());
        p.setId(yubi.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "yubikeyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer yubikeyAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            if (yubi.getClientId() > 0 && StringUtils.isNotBlank(yubi.getSecretKey())) {
                plan.registerAuthenticationHandler(yubikeyAuthenticationHandler());
                plan.registerAuthenticationMetadataPopulator(yubikeyAuthenticationMetaDataPopulator());
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(YubiKeyCredential.class));
            }
        };
    }
}
