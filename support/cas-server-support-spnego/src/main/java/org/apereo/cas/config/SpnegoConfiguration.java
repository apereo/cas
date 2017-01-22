package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsConfig;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.handler.support.NtlmAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver;
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
    public Authentication spnegoAuthentication() {
        return new Authentication();
    }
    
    @Bean
    @RefreshScope
    public JcifsConfig jcifsConfig() {
        final JcifsConfig c = new JcifsConfig();
        final SpnegoProperties spnego = casProperties.getAuthn().getSpnego();
        c.setJcifsDomain(spnego.getJcifsDomain());
        c.setJcifsDomainController(spnego.getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(spnego.getCachePolicy());
        c.setJcifsNetbiosWins(spnego.getJcifsNetbiosWins());
        c.setJcifsPassword(spnego.getJcifsPassword());
        c.setJcifsServicePassword(spnego.getJcifsServicePassword());
        c.setJcifsServicePrincipal(spnego.getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(spnego.getTimeout());
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
    public AuthenticationHandler spnegoHandler() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler(spnegoAuthentication(), spnegoProperties.isPrincipalWithDomainName(),
                spnegoProperties.isNtlmAllowed());
        h.setPrincipalFactory(spnegoPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(spnegoProperties.isPrincipalWithDomainName());
        h.setNTLMallowed(spnegoProperties.isNtlmAllowed());
        h.setName(spnegoProperties.getName());
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmProperties ntlmProperties = casProperties.getAuthn().getNtlm();
        final NtlmAuthenticationHandler ntlm = new NtlmAuthenticationHandler(ntlmProperties.isLoadBalance(), ntlmProperties.getDomainController(),
                ntlmProperties.getIncludePattern());
        ntlm.setPrincipalFactory(ntlmPrincipalFactory());
        ntlm.setServicesManager(servicesManager);
        ntlm.setName(ntlmProperties.getName());
        return ntlm;
    }

    @ConditionalOnMissingBean(name = "ntlmPrincipalFactory")
    @Bean
    public PrincipalFactory ntlmPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver spnegoPrincipalResolver() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final SpnegoPrincipalResolver r = new SpnegoPrincipalResolver();
        r.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(spnegoProperties.getPrincipalTransformation()));
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(spnegoProperties.getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(spnegoProperties.getPrincipal().isReturnNull());
        r.setPrincipalFactory(spnegoPrincipalFactory());
        return r;
    }

    @ConditionalOnMissingBean(name = "spnegoPrincipalFactory")
    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    /**
     * The type Spnego authentication event execution plan configuration.
     */
    @Configuration("spnegoAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class SpnegoAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(spnegoHandler(), spnegoPrincipalResolver());
        }
    }
}
