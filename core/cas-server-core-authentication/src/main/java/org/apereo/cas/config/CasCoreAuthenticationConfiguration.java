package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionFactory;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationManager;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration(value = "casCoreAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationConfiguration {

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Bean
    @RefreshScope
    @Autowired
    @ConditionalOnMissingBean(name = "authenticationTransactionManager")
    public AuthenticationTransactionManager authenticationTransactionManager(@Qualifier("casAuthenticationManager")
                                                                             final AuthenticationManager casAuthenticationManager) {
        return new DefaultAuthenticationTransactionManager(applicationContext, casAuthenticationManager);
    }

    @ConditionalOnMissingBean(name = "casAuthenticationManager")
    @Bean
    @RefreshScope
    public AuthenticationManager casAuthenticationManager() {
        return new DefaultAuthenticationManager(
            authenticationEventExecutionPlan.getObject(),
            casProperties.getPersonDirectory().getPrincipalResolutionFailureFatal() == TriStateBoolean.TRUE,
            applicationContext
        );
    }

    @ConditionalOnMissingBean(name = "authenticationResultBuilderFactory")
    @Bean
    @RefreshScope
    public AuthenticationResultBuilderFactory authenticationResultBuilderFactory() {
        return new DefaultAuthenticationResultBuilderFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationTransactionFactory")
    @Bean
    @RefreshScope
    public AuthenticationTransactionFactory authenticationTransactionFactory() {
        return new DefaultAuthenticationTransactionFactory();
    }
    
    @ConditionalOnMissingBean(name = AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    @Autowired
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlan authenticationEventExecutionPlan(final List<AuthenticationEventExecutionPlanConfigurer> configurers) {
        val plan = new DefaultAuthenticationEventExecutionPlan(authenticationSystemSupport.getObject());
        val sortedConfigurers = new ArrayList<>(configurers);
        AnnotationAwareOrderComparator.sortIfNecessary(sortedConfigurers);

        sortedConfigurers.forEach(Unchecked.consumer(c -> {
            LOGGER.trace("Configuring authentication execution plan [{}]", c.getName());
            c.configureAuthenticationExecutionPlan(plan);
        }));
        return plan;
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeReleasePolicy")
    @RefreshScope
    @Bean
    public AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy() {
        val release = casProperties.getAuthn().getAuthenticationAttributeRelease();
        if (!release.isEnabled()) {
            LOGGER.debug("CAS is configured to not release protocol-level authentication attributes.");
            return AuthenticationAttributeReleasePolicy.none();
        }
        return new DefaultAuthenticationAttributeReleasePolicy(release.getOnlyRelease(),
            release.getNeverRelease(),
            casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute());
    }
}
