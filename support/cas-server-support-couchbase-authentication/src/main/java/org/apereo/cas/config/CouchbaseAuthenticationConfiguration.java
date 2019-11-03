package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CouchbaseAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.support.CouchbasePersonAttributeDao;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchbaseAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("couchbaseAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CouchbaseAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "couchbasePrincipalFactory")
    @Bean
    public PrincipalFactory couchbasePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationCouchbaseClientFactory")
    @RefreshScope
    @Bean
    public CouchbaseClientFactory authenticationCouchbaseClientFactory() {
        val couchbase = casProperties.getAuthn().getCouchbase();
        val nodes = org.springframework.util.StringUtils.commaDelimitedListToSet(couchbase.getNodeSet());
        return new CouchbaseClientFactory(nodes, couchbase.getBucket(), couchbase.getPassword());
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler couchbaseAuthenticationHandler() {
        val couchbase = casProperties.getAuthn().getCouchbase();
        val handler = new CouchbaseAuthenticationHandler(
            servicesManager.getObject(), couchbasePrincipalFactory(),
            authenticationCouchbaseClientFactory(),
            couchbase);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(couchbase.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(couchbase.getPasswordEncoder()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer couchbaseAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val couchbase = casProperties.getAuthn().getCouchbase();
            if (StringUtils.isNotBlank(couchbase.getPasswordAttribute()) && StringUtils.isNotBlank(couchbase.getUsernameAttribute())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(couchbaseAuthenticationHandler(), defaultPrincipalResolver.getObject());
            } else {
                LOGGER.debug("No couchbase username/password is defined, so couchbase authentication will not be registered in the execution plan");
            }
        };
    }

    @ConditionalOnMissingBean(name = "couchbasePersonAttributeDao")
    @Bean
    public IPersonAttributeDao couchbasePersonAttributeDao() {
        val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
        val cb = new CouchbasePersonAttributeDao(couchbase, authenticationCouchbaseClientFactory());
        cb.setOrder(couchbase.getOrder());
        FunctionUtils.doIfNotNull(couchbase.getId(), cb::setId);
        return cb;
    }

    @ConditionalOnMissingBean(name = "couchbaseAttributeRepositoryPlanConfigurer")
    @Bean
    public PersonDirectoryAttributeRepositoryPlanConfigurer couchbaseAttributeRepositoryPlanConfigurer() {
        val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
        return plan -> {
            if (StringUtils.isNotBlank(couchbase.getUsernameAttribute())) {
                plan.registerAttributeRepository(couchbasePersonAttributeDao());
            }
        };
    }
}
