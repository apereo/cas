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
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "spnegoAuthentication")
    public Authentication spnegoAuthentication() {
        return new Authentication();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "jcifsConfig")
    public JcifsConfig jcifsConfig() {
        val c = new JcifsConfig();
        val spnego = casProperties.getAuthn().getSpnego();
        c.setJcifsDomain(spnego.getJcifsDomain());
        c.setJcifsDomainController(spnego.getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(spnego.getCachePolicy());
        c.setJcifsNetbiosWins(spnego.getJcifsNetbiosWins());
        c.setJcifsPassword(spnego.getJcifsPassword());
        c.setJcifsServicePassword(spnego.getJcifsServicePassword());
        c.setJcifsServicePrincipal(spnego.getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(Beans.newDuration(spnego.getTimeout()).toMillis());
        c.setJcifsUsername(spnego.getJcifsUsername());
        c.setKerberosConf(spnego.getKerberosConf());
        c.setKerberosDebug(spnego.getKerberosDebug());
        c.setKerberosKdc(spnego.getKerberosKdc());
        c.setKerberosRealm(spnego.getKerberosRealm());
        c.setLoginConf(spnego.getLoginConf());
        c.setUseSubjectCredsOnly(spnego.isUseSubjectCredsOnly());

        return c;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "spnegoHandler")
    public AuthenticationHandler spnegoHandler() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        val h = new JcifsSpnegoAuthenticationHandler(spnegoProperties.getName(), servicesManager, spnegoPrincipalFactory(),
            spnegoAuthentication(), spnegoProperties.isPrincipalWithDomainName(), spnegoProperties.isNtlmAllowed());
        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(spnegoProperties.isPrincipalWithDomainName());
        h.setNtlmAllowed(spnegoProperties.isNtlmAllowed());
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        val ntlmProperties = casProperties.getAuthn().getNtlm();
        return new NtlmAuthenticationHandler(ntlmProperties.getName(), servicesManager, ntlmPrincipalFactory(),
            ntlmProperties.isLoadBalance(),
            ntlmProperties.getDomainController(), ntlmProperties.getIncludePattern());
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
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new SpnegoPrincipalResolver(attributeRepository, spnegoPrincipalFactory(),
            spnegoProperties.getPrincipal().isReturnNull(),
            PrincipalNameTransformerUtils.newPrincipalNameTransformer(spnegoProperties.getPrincipalTransformation()),
            spnegoProperties.getPrincipal().getPrincipalAttribute());
    }

    @ConditionalOnMissingBean(name = "spnegoPrincipalFactory")
    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "spnegoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer spnegoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(spnegoHandler(), spnegoPrincipalResolver());
    }
}
