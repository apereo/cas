package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
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
@Slf4j
public class CasCoreAuthenticationPrincipalConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("attributeRepositories")
    private List<IPersonAttributeDao> attributeRepositories;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @ConditionalOnMissingBean(name = "principalElectionStrategy")
    @Bean
    @RefreshScope
    public PrincipalElectionStrategy principalElectionStrategy() {
        return new DefaultPrincipalElectionStrategy(principalFactory());
    }

    @ConditionalOnMissingBean(name = "principalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory principalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "personDirectoryPrincipalResolver")
    public PrincipalResolver personDirectoryPrincipalResolver() {
        final PersonDirectoryPrincipalResolverProperties personDirectory = casProperties.getPersonDirectory();
        final PersonDirectoryPrincipalResolver bean = new PersonDirectoryPrincipalResolver(
            attributeRepository,
            principalFactory(),
            personDirectory.isReturnNull(),
            personDirectory.getPrincipalAttribute()
        );
        bean.setUseCurrentPrincipalId(personDirectory.isUseExistingPrincipalId());

        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        if (!attributeRepositories.isEmpty()) {
            LOGGER.debug("Attribute repository sources are defined and available for the principal resolution chain. "
                + "The principal resolver will use a combination of attributes collected from attribute repository sources "
                + "and whatever may be collected during the authentication phase where results are eventually merged.");
            resolver.setChain(CollectionUtils.wrapList(new EchoingPrincipalResolver(), bean));
        } else {
            LOGGER.debug("Attribute repository sources are not available for principal resolution so principal resolver will echo "
                + "back the principal resolved during authentication directly.");
            resolver.setChain(CollectionUtils.wrapList(new EchoingPrincipalResolver()));
        }

        return resolver;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "globalPrincipalAttributeRepository")
    public PrincipalAttributesRepository globalPrincipalAttributeRepository() {
        final PrincipalAttributesProperties props = casProperties.getAuthn().getAttributeRepository();
        final long cacheTime = props.getExpirationTime();
        if (cacheTime < 0) {
            return new DefaultPrincipalAttributesRepository();
        }
        return new CachingPrincipalAttributesRepository(props.getExpirationTimeUnit().toUpperCase(), cacheTime);
    }
}
