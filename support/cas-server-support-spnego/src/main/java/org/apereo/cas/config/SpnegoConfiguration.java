package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

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
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

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

        c.setJcifsDomain(casProperties.getAuthn().getSpnego().getJcifsDomain());
        c.setJcifsDomainController(casProperties.getAuthn().getSpnego().getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(casProperties.getAuthn().getSpnego().getCachePolicy());
        c.setJcifsNetbiosWins(casProperties.getAuthn().getSpnego().getJcifsNetbiosWins());
        c.setJcifsPassword(casProperties.getAuthn().getSpnego().getJcifsPassword());
        c.setJcifsServicePassword(casProperties.getAuthn().getSpnego().getJcifsServicePassword());
        c.setJcifsServicePrincipal(casProperties.getAuthn().getSpnego().getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(casProperties.getAuthn().getSpnego().getTimeout());
        c.setJcifsUsername(casProperties.getAuthn().getSpnego().getJcifsUsername());
        c.setKerberosConf(casProperties.getAuthn().getSpnego().getKerberosConf());
        c.setKerberosDebug(casProperties.getAuthn().getSpnego().getKerberosDebug());
        c.setKerberosKdc(casProperties.getAuthn().getSpnego().getKerberosKdc());
        c.setKerberosRealm(casProperties.getAuthn().getSpnego().getKerberosRealm());
        c.setLoginConf(casProperties.getAuthn().getSpnego().getLoginConf());
        c.setUseSubjectCredsOnly(casProperties.getAuthn().getSpnego().isUseSubjectCredsOnly());

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

    @Bean
    public PrincipalFactory ntlmPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver spnegoPrincipalResolver() {
        final SpnegoPrincipalResolver r = new SpnegoPrincipalResolver();
        r.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getSpnego().getPrincipalTransformation()));
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getSpnego().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getSpnego().getPrincipal().isReturnNull());
        r.setPrincipalFactory(spnegoPrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    
    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(spnegoHandler(), spnegoPrincipalResolver());
    }
}
