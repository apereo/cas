package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationPrincipalConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationPrincipalConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationPrincipalConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "principalElectionStrategy")
    @Autowired
    @Bean
    public PrincipalElectionStrategy principalElectionStrategy(@Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        return new DefaultPrincipalElectionStrategy(principalFactory);
    }

    @ConditionalOnMissingBean(name = "principalFactory")
    @Bean
    public PrincipalFactory principalFactory() {
        return new DefaultPrincipalFactory();
    }
    
    @Autowired
    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "personDirectoryPrincipalResolver")
    public PrincipalResolver personDirectoryPrincipalResolver(@Qualifier("attributeRepository") final IPersonAttributeDao attributeRepository,
                                                              @Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        final PersonDirectoryPrincipalResolver bean = new PersonDirectoryPrincipalResolver();
        bean.setAttributeRepository(attributeRepository);
        bean.setPrincipalAttributeName(casProperties.getPersonDirectory().getPrincipalAttribute());
        bean.setReturnNullIfNoAttributes(casProperties.getPersonDirectory().isReturnNull());
        bean.setPrincipalFactory(principalFactory);
        return bean;
    }
}
