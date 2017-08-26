package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasCoreAuthenticationPrincipalConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationPrincipalConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationPrincipalConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreAuthenticationPrincipalConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("attributeRepositories")
    private List<IPersonAttributeDao> attributeRepositories;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

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
    public PrincipalResolver personDirectoryPrincipalResolver(@Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        final PersonDirectoryPrincipalResolver bean = new PersonDirectoryPrincipalResolver(
                attributeRepository, 
                principalFactory,
                casProperties.getPersonDirectory().isReturnNull(),
                casProperties.getPersonDirectory().getPrincipalAttribute()
        );
        
        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        if (!attributeRepositories.isEmpty()) {
            LOGGER.debug("Attribute repository sources are defined and available for the principal resolution chain. "
                    + "The principal resolver will use a combination of attributes collected from attribute repository sources "
                    + "and whatever may be collected during the authentication phase where results are eventually merged.");
            resolver.setChain(CollectionUtils.wrapList(bean, new EchoingPrincipalResolver()));
        } else {
            LOGGER.debug("Attribute repository sources are not available for principal resolution so principal resolver will echo "
                    + "back the principal resolved during authentication directly.");
            resolver.setChain(new EchoingPrincipalResolver());
        }

        return resolver;
    }
}
