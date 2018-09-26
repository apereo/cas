package org.apereo.cas.support.pac4j.config.support.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.DelegatedAuthenticationAuditResourceResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Set;

/**
 * This is {@link Pac4jAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("pac4jAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class Pac4jAuthenticationEventExecutionPlanConfiguration implements AuditTrailRecordResolutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationActionResolver")
    private AuditActionResolver authenticationActionResolver;

    @Bean
    @ConditionalOnMissingBean(name = "pac4jDelegatedClientFactory")
    @RefreshScope
    public DelegatedClientFactory pac4jDelegatedClientFactory() {
        return new DelegatedClientFactory(casProperties.getAuthn().getPac4j());
    }

    @RefreshScope
    @Bean
    public Clients builtClients() {
        final Set<BaseClient> clients = pac4jDelegatedClientFactory().build();
        LOGGER.debug("The following clients are built: [{}]", clients);
        if (clients.isEmpty()) {
            LOGGER.warn("No delegated authentication clients are defined and/or configured");
        } else {
            LOGGER.info("Located and prepared [{}] delegated authentication client(s)", clients.size());
        }
        return new Clients(casProperties.getServer().getLoginUrl(), new ArrayList<>(clients));
    }

    @ConditionalOnMissingBean(name = "clientPrincipalFactory")
    @Bean
    public PrincipalFactory clientPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "clientAuthenticationMetaDataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
        return new ClientAuthenticationMetaDataPopulator();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "clientAuthenticationHandler")
    public AuthenticationHandler clientAuthenticationHandler() {
        final Pac4jDelegatedAuthenticationProperties pac4j = casProperties.getAuthn().getPac4j();
        final ClientAuthenticationHandler h = new ClientAuthenticationHandler(pac4j.getName(), servicesManager,
            clientPrincipalFactory(),
            builtClients());
        h.setTypedIdUsed(pac4j.isTypedIdUsed());
        h.setPrincipalAttributeId(pac4j.getPrincipalAttributeId());
        return h;
    }

    @ConditionalOnMissingBean(name = "pac4jAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (!builtClients().findAllClients().isEmpty()) {
                LOGGER.info("Registering delegated authentication clients...");
                plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler(), personDirectoryPrincipalResolver);
                plan.registerMetadataPopulator(clientAuthenticationMetaDataPopulator());
            }
        };
    }

    @ConditionalOnMissingBean(name = "delegatedAuthenticationAuditResourceResolver")
    @Bean
    public AuditResourceResolver delegatedAuthenticationAuditResourceResolver() {
        return new DelegatedAuthenticationAuditResourceResolver();
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditActionResolver("DELEGATED_CLIENT_ACTION_RESOLVER", this.authenticationActionResolver);
        plan.registerAuditResourceResolver("DELEGATED_CLIENT_RESOURCE_RESOLVER", delegatedAuthenticationAuditResourceResolver());
    }
}
