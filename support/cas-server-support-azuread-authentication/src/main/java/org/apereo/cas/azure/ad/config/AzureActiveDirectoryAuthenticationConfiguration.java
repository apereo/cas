package org.apereo.cas.azure.ad.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.azure.ad.authentication.AzureActiveDirectoryAuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.MicrosoftGraphPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AzureActiveDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "azureActiveDirectoryAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AzureActiveDirectoryAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        final AuthenticationHandler handler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer")
    @Bean
    @Autowired
    public PersonDirectoryAttributeRepositoryPlanConfigurer microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("microsoftAzureActiveDirectoryAttributeRepositories")
        final List<IPersonAttributeDao> repositories) {
        return plan -> repositories.forEach(plan::registerAttributeRepository);
    }

    @Configuration(value = "AzureActiveDirectoryAuthenticationInternalConfiguration", proxyBeanMethods = false)
    public static class AzureActiveDirectoryAuthenticationInternalConfiguration {

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public List<IPersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            val attrs = casProperties.getAuthn().getAttributeRepository();
            attrs.getAzureActiveDirectory().stream()
                .filter(msft -> StringUtils.isNotBlank(msft.getClientId()) && StringUtils.isNotBlank(msft.getClientSecret()))
                .forEach(msft -> {
                    val dao = new MicrosoftGraphPersonAttributeDao();
                    FunctionUtils.doIfNotNull(msft.getId(), dao::setId);
                    FunctionUtils.doIfNotNull(msft.getDomain(), dao::setDomain);
                    FunctionUtils.doIfNotNull(msft.getApiBaseUrl(), dao::setApiBaseUrl);
                    FunctionUtils.doIfNotNull(msft.getGrantType(), dao::setGrantType);
                    FunctionUtils.doIfNotNull(msft.getLoginBaseUrl(), dao::setLoginBaseUrl);
                    FunctionUtils.doIfNotNull(msft.getLoggingLevel(), dao::setLoggingLevel);
                    FunctionUtils.doIfNotNull(msft.getAttributes(), dao::setProperties);
                    FunctionUtils.doIfNotNull(msft.getResource(), dao::setResource);
                    FunctionUtils.doIfNotNull(msft.getScope(), dao::setScope);
                    FunctionUtils.doIfNotNull(msft.getTenant(), dao::setTenant);
                    dao.setClientSecret(msft.getClientSecret());
                    dao.setClientId(msft.getClientId());
                    dao.setOrder(msft.getOrder());
                    list.add(dao);
                });
            return list;
        }

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryPrincipalFactory")
        @Bean
        public PrincipalFactory microsoftAzureActiveDirectoryPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("microsoftAzureActiveDirectoryPrincipalFactory")
            final PrincipalFactory factory) {
            val azure = casProperties.getAuthn().getAzureActiveDirectory();
            val handler = new AzureActiveDirectoryAuthenticationHandler(azure.getName(),
                servicesManager, factory, azure.getOrder(), azure.getClientId(), azure.getLoginUrl(),
                azure.getResource());
            handler.setState(azure.getState());
            handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(azure.getPrincipalTransformation()));
            handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(azure.getPasswordEncoder(), applicationContext));
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(azure.getCredentialCriteria()));
            return handler;
        }
    }
}
