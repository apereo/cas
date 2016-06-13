package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyApplicationContextWrapper;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yubikeyConfiguration")
public class YubiKeyConfiguration {

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("builder")
    private FlowBuilderServices builder;

    @Autowired(required = false)
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry registry;

    @Bean
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder =
                new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-yubikey/*-webflow.xml");
        return builder.build();
    }

    @Bean
    public BaseApplicationContextWrapper yubiKeyApplicationContextWrapper() {
        final YubiKeyApplicationContextWrapper y = new YubiKeyApplicationContextWrapper();

        y.setAuthenticationHandler(yubikeyAuthenticationHandler());
        y.setPopulator(yubikeyAuthenticationMetaDataPopulator());

        return y;
    }

    @Bean
    @RefreshScope
    public YubiKeyAuthenticationHandler yubikeyAuthenticationHandler() {

        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(
                this.casProperties.getAuthn().getMfa().getYubikey().getClientId(),
                this.casProperties.getAuthn().getMfa().getYubikey().getSecretKey());

        if (registry != null) {
            handler.setRegistry(this.registry);
        }
        return handler;
    }

    @Bean
    @RefreshScope
    public YubiKeyAuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        final YubiKeyAuthenticationMetaDataPopulator pop =
                new YubiKeyAuthenticationMetaDataPopulator();

        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(yubikeyAuthenticationHandler());
        pop.setProvider(yubikeyAuthenticationProvider());
        return pop;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        return new YubiKeyMultifactorAuthenticationProvider(
                yubikeyAuthenticationHandler(),
                this.casProperties.getAuthn().getMfa().getYubikey().getRank(),
                this.httpClient);
    }

    @Bean
    public Action yubikeyAuthenticationWebflowAction() {
        final YubiKeyAuthenticationWebflowAction a = new YubiKeyAuthenticationWebflowAction();
        a.setYubikeyAuthenticationWebflowEventResolver(yubikeyAuthenticationWebflowEventResolver());
        return a;
    }

    @Bean
    public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer() {
        final YubiKeyMultifactorWebflowConfigurer c = new YubiKeyMultifactorWebflowConfigurer();
        c.setYubikeyFlowRegistry(yubikeyFlowRegistry());
        return c;
    }

    @Bean
    public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver() {
        return new YubiKeyAuthenticationWebflowEventResolver();
    }
}
