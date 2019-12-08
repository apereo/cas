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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AzureActiveDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "azureActiveDirectoryAuthenticationConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AzureActiveDirectoryAuthenticationConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer")
    @Bean
    public PersonDirectoryAttributeRepositoryPlanConfigurer microsoftAzureActiveDirectoryAttributeRepositoryPlanConfigurer() {
        return plan -> microsoftAzureActiveDirectoryAttributeRepositories().forEach(plan::registerAttributeRepository);
    }

    @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories() {
        val list = new ArrayList<IPersonAttributeDao>();
        val attrs = casProperties.getAuthn().getAttributeRepository();
        attrs.getAzureActiveDirectory()
            .stream()
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
    @RefreshScope
    public AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler() {
        val azure = casProperties.getAuthn().getAzureActiveDirectory();
        val handler = new AzureActiveDirectoryAuthenticationHandler(azure.getName(),
            servicesManager.getObject(),
            microsoftAzureActiveDirectoryPrincipalFactory(),
            azure.getOrder(),
            azure.getClientId(),
            azure.getLoginUrl(),
            azure.getResource());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(azure.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(azure.getPasswordEncoder(), applicationContext));
        handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(azure.getCredentialCriteria()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer microsoftAzureActiveDirectoryAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
            microsoftAzureActiveDirectoryAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }
}
