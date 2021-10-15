package org.apereo.cas.config;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.jaas.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.ProxyingPrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasCoreAuthenticationHandlersConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "CasCoreAuthenticationHandlersConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationHandlersConfiguration {

    @Configuration(value = "CasCoreAuthenticationHandlersProxyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public static class CasCoreAuthenticationHandlersProxyConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationHandler proxyAuthenticationHandler(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("proxyPrincipalFactory")
            final PrincipalFactory proxyPrincipalFactory,
            @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
            final HttpClient supportsTrustStoreSslSocketFactoryHttpClient) {
            return new HttpBasedServiceCredentialsAuthenticationHandler(null,
                servicesManager, proxyPrincipalFactory, Integer.MIN_VALUE,
                supportsTrustStoreSslSocketFactoryHttpClient);
        }

        @ConditionalOnMissingBean(name = "proxyPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory proxyPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "proxyPrincipalResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalResolver proxyPrincipalResolver(
            @Qualifier("proxyPrincipalFactory")
            final PrincipalFactory proxyPrincipalFactory) {
            return new ProxyingPrincipalResolver(proxyPrincipalFactory);
        }

        @ConditionalOnMissingBean(name = "proxyAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer proxyAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("proxyAuthenticationHandler")
            final AuthenticationHandler proxyAuthenticationHandler,
            @Qualifier("proxyPrincipalResolver")
            final PrincipalResolver proxyPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(proxyAuthenticationHandler, proxyPrincipalResolver);
        }

    }

    @Configuration(value = "CasCoreAuthenticationHandlersAcceptConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationHandlersAcceptConfiguration {

        private static Map<String, String> getParsedUsers(final CasConfigurationProperties casProperties) {
            val accept = casProperties.getAuthn().getAccept();
            val usersProperty = accept.getUsers();
            if (accept.isEnabled() && StringUtils.isNotBlank(usersProperty) && usersProperty.contains("::")) {
                val pattern = Pattern.compile("::");
                return Stream.of(usersProperty.split(","))
                    .map(pattern::split)
                    .collect(Collectors.toMap(userAndPassword -> userAndPassword[0], userAndPassword -> userAndPassword[1]));
            }
            return new HashMap<>(0);
        }


        @ConditionalOnMissingBean(name = "acceptPasswordPolicyConfiguration")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordPolicyContext acceptPasswordPolicyConfiguration() {
            return new PasswordPolicyContext();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "acceptUsersAuthenticationHandler")
        @Autowired
        public AuthenticationHandler acceptUsersAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("acceptUsersPrincipalFactory")
            final PrincipalFactory acceptUsersPrincipalFactory,
            @Qualifier("acceptPasswordPolicyConfiguration")
            final PasswordPolicyContext acceptPasswordPolicyConfiguration) {
            val props = casProperties.getAuthn().getAccept();
            val h = new AcceptUsersAuthenticationHandler(props.getName(),
                servicesManager, acceptUsersPrincipalFactory, props.getOrder(), getParsedUsers(casProperties));
            h.setState(props.getState());
            h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(props.getPasswordEncoder(), applicationContext));
            h.setPasswordPolicyConfiguration(acceptPasswordPolicyConfiguration);
            h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(props.getCredentialCriteria()));
            h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(props.getPrincipalTransformation()));
            val passwordPolicy = props.getPasswordPolicy();
            h.setPasswordPolicyHandlingStrategy(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(passwordPolicy, applicationContext));
            if (passwordPolicy.isEnabled()) {
                val cfg = new PasswordPolicyContext(passwordPolicy);
                if (passwordPolicy.isAccountStateHandlingEnabled()) {
                    cfg.setAccountStateHandler((response, configuration) -> new ArrayList<>(0));
                } else {
                    LOGGER.debug("Handling account states is disabled via CAS configuration");
                }
                h.setPasswordPolicyConfiguration(cfg);
            }
            return h;
        }

        @ConditionalOnMissingBean(name = "acceptUsersPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory acceptUsersPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "CasCoreAuthenticationHandlersJaasConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationHandlersJaasConfiguration {

        @ConditionalOnMissingBean(name = "jaasPasswordPolicyConfiguration")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordPolicyContext jaasPasswordPolicyConfiguration() {
            return new PasswordPolicyContext();
        }

        @ConditionalOnMissingBean(name = "jaasPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory jaasPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @ConditionalOnMissingBean(name = "jaasPersonDirectoryPrincipalResolvers")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public List<PrincipalResolver> jaasPersonDirectoryPrincipalResolvers(
            final CasConfigurationProperties casProperties,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository,
            @Qualifier("jaasPrincipalFactory")
            final PrincipalFactory jaasPrincipalFactory) {
            val personDirectory = casProperties.getPersonDirectory();
            return casProperties.getAuthn().getJaas()
                .stream()
                .filter(jaas -> StringUtils.isNotBlank(jaas.getRealm()))
                .map(jaas -> {
                    val jaasPrincipal = jaas.getPrincipal();
                    var attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
                    return CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(jaasPrincipalFactory,
                        attributeRepository, attributeMerger, jaasPrincipal, personDirectory);
                })
                .collect(Collectors.toList());
        }

        @ConditionalOnMissingBean(name = "jaasAuthenticationHandlers")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public List<AuthenticationHandler> jaasAuthenticationHandlers(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("jaasPrincipalFactory")
            final PrincipalFactory jaasPrincipalFactory) {
            return casProperties.getAuthn().getJaas()
                .stream()
                .filter(jaas -> StringUtils.isNotBlank(jaas.getRealm()))
                .map(jaas -> {
                    val h = new JaasAuthenticationHandler(jaas.getName(), servicesManager, jaasPrincipalFactory, jaas.getOrder());
                    h.setState(jaas.getState());
                    h.setKerberosKdcSystemProperty(jaas.getKerberosKdcSystemProperty());
                    h.setKerberosRealmSystemProperty(jaas.getKerberosRealmSystemProperty());
                    h.setRealm(jaas.getRealm());
                    h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(jaas.getPasswordEncoder(), applicationContext));

                    if (StringUtils.isNotBlank(jaas.getLoginConfigType())) {
                        h.setLoginConfigType(jaas.getLoginConfigType());
                    }
                    if (StringUtils.isNotBlank(jaas.getLoginConfigurationFile())) {
                        h.setLoginConfigurationFile(new File(jaas.getLoginConfigurationFile()));
                    }
                    val passwordPolicy = jaas.getPasswordPolicy();
                    h.setPasswordPolicyHandlingStrategy(CoreAuthenticationUtils.newPasswordPolicyHandlingStrategy(passwordPolicy, applicationContext));
                    if (passwordPolicy.isEnabled()) {
                        LOGGER.debug("Password policy is enabled for JAAS. Constructing password policy configuration for [{}]", jaas.getRealm());
                        val cfg = new PasswordPolicyContext(passwordPolicy);
                        if (passwordPolicy.isAccountStateHandlingEnabled()) {
                            cfg.setAccountStateHandler((response, configuration) -> new ArrayList<>(0));
                        } else {
                            LOGGER.debug("Handling account states is disabled via CAS configuration");
                        }
                        h.setPasswordPolicyConfiguration(cfg);
                    }
                    h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(jaas.getPrincipalTransformation()));
                    h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(jaas.getCredentialCriteria()));
                    return h;
                })
                .collect(Collectors.toList());
        }

        @ConditionalOnMissingBean(name = "jaasAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer jaasAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("jaasAuthenticationHandlers")
            final List<AuthenticationHandler> jaasAuthenticationHandlers,
            @Qualifier("jaasPersonDirectoryPrincipalResolvers")
            final List<PrincipalResolver> jaasPersonDirectoryPrincipalResolvers) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolvers(jaasAuthenticationHandlers,
                jaasPersonDirectoryPrincipalResolvers);
        }
    }
}
