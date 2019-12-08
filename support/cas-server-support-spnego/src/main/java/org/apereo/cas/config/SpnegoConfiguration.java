package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsConfig;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.handler.support.NtlmAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver;

import jcifs.spnego.Authentication;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link SpnegoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("spnegoConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private ObjectProvider<IPersonAttributeDao> attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "spnegoAuthentications")
    public List<Authentication> spnegoAuthentications() {
        val spnegoSystem = casProperties.getAuthn().getSpnego().getSystem();

        JcifsConfig.SystemSettings.initialize(resourceLoader, spnegoSystem.getLoginConf());
        JcifsConfig.SystemSettings.setKerberosConf(spnegoSystem.getKerberosConf());
        JcifsConfig.SystemSettings.setKerberosDebug(spnegoSystem.getKerberosDebug());
        JcifsConfig.SystemSettings.setKerberosKdc(spnegoSystem.getKerberosKdc());
        JcifsConfig.SystemSettings.setKerberosRealm(spnegoSystem.getKerberosRealm());
        JcifsConfig.SystemSettings.setUseSubjectCredsOnly(spnegoSystem.isUseSubjectCredsOnly());

        val props = casProperties.getAuthn().getSpnego().getProperties();
        return props.stream()
            .map(p -> {
                val c = new JcifsConfig();
                val jcifsSettings = c.getJcifsSettings();
                jcifsSettings.setJcifsDomain(p.getJcifsDomain());
                jcifsSettings.setJcifsDomainController(p.getJcifsDomainController());
                jcifsSettings.setJcifsNetbiosCachePolicy(p.getCachePolicy());
                jcifsSettings.setJcifsNetbiosWins(p.getJcifsNetbiosWins());
                jcifsSettings.setJcifsPassword(p.getJcifsPassword());
                jcifsSettings.setJcifsServicePassword(p.getJcifsServicePassword());
                jcifsSettings.setJcifsServicePrincipal(p.getJcifsServicePrincipal());
                jcifsSettings.setJcifsSocketTimeout(Beans.newDuration(p.getTimeout()).toMillis());
                jcifsSettings.setJcifsUsername(p.getJcifsUsername());
                return new Authentication(jcifsSettings.getProperties());
            })
            .collect(Collectors.toList());
    }


    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "spnegoHandler")
    public AuthenticationHandler spnegoHandler() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new JcifsSpnegoAuthenticationHandler(spnegoProperties.getName(),
            servicesManager.getObject(),
            spnegoPrincipalFactory(),
            spnegoAuthentications(),
            spnegoProperties.isPrincipalWithDomainName(),
            spnegoProperties.isNtlmAllowed(),
            spnegoProperties.getOrder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnProperty(prefix = "cas.authn.ntlm", name = "enabled", havingValue = "true")
    public AuthenticationHandler ntlmAuthenticationHandler() {
        val ntlmProperties = casProperties.getAuthn().getNtlm();
        return new NtlmAuthenticationHandler(ntlmProperties.getName(),
            servicesManager.getObject(), ntlmPrincipalFactory(),
            ntlmProperties.isLoadBalance(),
            ntlmProperties.getDomainController(),
            ntlmProperties.getIncludePattern(),
            ntlmProperties.getOrder());
    }

    @ConditionalOnMissingBean(name = "ntlmPrincipalFactory")
    @Bean
    public PrincipalFactory ntlmPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "spnegoPrincipalResolver")
    public PrincipalResolver spnegoPrincipalResolver() {
        val personDirectory = casProperties.getPersonDirectory();
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        val spnegoPrincipal = spnegoProperties.getPrincipal();
        val principalAttribute = StringUtils.defaultIfBlank(spnegoPrincipal.getPrincipalAttribute(), personDirectory.getPrincipalAttribute());
        return new SpnegoPrincipalResolver(attributeRepository.getObject(),
            spnegoPrincipalFactory(),
            spnegoPrincipal.isReturnNull() || personDirectory.isReturnNull(),
            PrincipalNameTransformerUtils.newPrincipalNameTransformer(spnegoProperties.getPrincipalTransformation()),
            principalAttribute,
            spnegoPrincipal.isUseExistingPrincipalId() || personDirectory.isUseExistingPrincipalId(),
            spnegoPrincipal.isAttributeResolutionEnabled(),
            org.springframework.util.StringUtils.commaDelimitedListToSet(spnegoPrincipal.getActiveAttributeRepositoryIds()));
    }

    @ConditionalOnMissingBean(name = "spnegoPrincipalFactory")
    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "spnegoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer spnegoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandlerWithPrincipalResolver(spnegoHandler(), spnegoPrincipalResolver());
            if (casProperties.getAuthn().getNtlm().isEnabled()) {
                plan.registerAuthenticationHandler(ntlmAuthenticationHandler());
            }
        };
    }
}
