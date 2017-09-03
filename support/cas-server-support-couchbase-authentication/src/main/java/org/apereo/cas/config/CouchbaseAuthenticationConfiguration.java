package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CouchbaseAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * This is {@link CouchbaseAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("couchbaseAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "couchbasePrincipalFactory")
    @Bean
    public PrincipalFactory couchbasePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationCouchbaseClientFactory")
    @RefreshScope
    @Bean
    public CouchbaseClientFactory authenticationCouchbaseClientFactory() {
        final CouchbaseAuthenticationProperties couchbase = casProperties.getAuthn().getCouchbase();
        final Set<String> nodes = StringUtils.commaDelimitedListToSet(couchbase.getNodeSet());
        return new CouchbaseClientFactory(nodes, couchbase.getBucket(), couchbase.getPassword());
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler couchbaseAuthenticationHandler() {
        final CouchbaseAuthenticationProperties couchbase = casProperties.getAuthn().getCouchbase();
        final CouchbaseAuthenticationHandler handler = new CouchbaseAuthenticationHandler(
                servicesManager, couchbasePrincipalFactory(),
                authenticationCouchbaseClientFactory(), couchbase);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(couchbase.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(couchbase.getPasswordEncoder()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer couchbaseAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(couchbaseAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
