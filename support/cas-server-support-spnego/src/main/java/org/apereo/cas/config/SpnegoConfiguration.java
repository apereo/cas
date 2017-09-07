package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
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
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler(spnegoProperties.getName(), servicesManager, spnegoPrincipalFactory(),
                spnegoAuthentication(), spnegoProperties.isPrincipalWithDomainName(), spnegoProperties.isNtlmAllowed());
        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(spnegoProperties.isPrincipalWithDomainName());
        h.setNTLMallowed(spnegoProperties.isNtlmAllowed());
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmProperties ntlmProperties = casProperties.getAuthn().getNtlm();
        return new NtlmAuthenticationHandler(ntlmProperties.getName(), servicesManager, ntlmPrincipalFactory(), ntlmProperties.isLoadBalance(),
                ntlmProperties.getDomainController(), ntlmProperties.getIncludePattern());
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
        return new SpnegoPrincipalResolver(attributeRepository, spnegoPrincipalFactory(),
                spnegoProperties.getPrincipal().isReturnNull(),
                PrincipalNameTransformerUtils.newPrincipalNameTransformer(spnegoProperties.getPrincipalTransformation()),
                spnegoProperties.getPrincipal().getPrincipalAttribute());
    }

    @ConditionalOnMissingBean(name = "spnegoPrincipalFactory")
    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "spnegoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer spnegoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(spnegoHandler(), spnegoPrincipalResolver());
    }
}
