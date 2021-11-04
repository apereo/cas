package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.ldap[0]", value = "ldap-url")
@Configuration(value = "CasPersonDirectoryLdapConfiguration", proxyBeanMethods = false)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryLdapConfiguration {

    @Configuration(value = "LdapAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class LdapAttributeRepositoryConfiguration {

        private static final LdapEntryHandler[] LDAP_ENTRY_HANDLERS = new LdapEntryHandler[0];

        private static final SearchResultHandler[] SEARCH_RESULT_HANDLERS = new SearchResultHandler[0];

        @ConditionalOnMissingBean(name = "ldapAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> ldapAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<IPersonAttributeDao>();
            val attrs = casProperties.getAuthn().getAttributeRepository();
            attrs.getLdap()
                .stream()
                .filter(ldap -> StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getLdapUrl()))
                .forEach(ldap -> {
                    val dao = new LdaptivePersonAttributeDao();
                    FunctionUtils.doIfNotNull(ldap.getId(), dao::setId);
                    LOGGER.debug("Configured LDAP attribute source for [{}] and baseDn [{}]", ldap.getLdapUrl(), ldap.getBaseDn());
                    dao.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
                    dao.setBaseDN(ldap.getBaseDn());
                    dao.setEnabled(ldap.getState() != AttributeRepositoryStates.DISABLED);
                    dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                        ldap.getState() == AttributeRepositoryStates.ACTIVE);

                    LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(), ldap.getSearchFilter());
                    dao.setSearchFilter(ldap.getSearchFilter());

                    val constraints = new SearchControls();
                    if (ldap.getAttributes() != null && !ldap.getAttributes().isEmpty()) {
                        LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", ldap.getLdapUrl(), ldap.getAttributes());
                        dao.setResultAttributeMapping(ldap.getAttributes());
                        val attributes = (String[]) ldap.getAttributes().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
                        constraints.setReturningAttributes(attributes);
                    } else {
                        LOGGER.debug("Retrieving all attributes as no explicit attribute mappings are defined for [{}]", ldap.getLdapUrl());
                        constraints.setReturningAttributes(null);
                    }

                    val binaryAttributes = ldap.getBinaryAttributes();
                    if (binaryAttributes != null && !binaryAttributes.isEmpty()) {
                        LOGGER.debug("Setting binary attributes [{}]", binaryAttributes);
                        dao.setBinaryAttributes(binaryAttributes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    }

                    val searchEntryHandlers = ldap.getSearchEntryHandlers();
                    if (searchEntryHandlers != null && !searchEntryHandlers.isEmpty()) {
                        val entryHandlers = LdapUtils.newLdaptiveEntryHandlers(searchEntryHandlers);
                        if (!entryHandlers.isEmpty()) {
                            LOGGER.debug("Setting entry handlers [{}]", entryHandlers);
                            dao.setEntryHandlers(entryHandlers.toArray(LDAP_ENTRY_HANDLERS));
                        }
                        val searchResultHandlers = LdapUtils.newLdaptiveSearchResultHandlers(searchEntryHandlers);
                        if (!searchResultHandlers.isEmpty()) {
                            LOGGER.debug("Setting search result handlers [{}]", searchResultHandlers);
                            dao.setSearchResultHandlers(searchResultHandlers.toArray(SEARCH_RESULT_HANDLERS));
                        }
                    }

                    if (ldap.isSubtreeSearch()) {
                        LOGGER.debug("Configured subtree searching for [{}]", ldap.getLdapUrl());
                        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    }
                    if (!ldap.getQueryAttributes().isEmpty()) {
                        dao.setQueryAttributeMapping(ldap.getQueryAttributes());
                    }
                    constraints.setDerefLinkFlag(true);
                    dao.setSearchControls(constraints);
                    dao.setUseAllQueryAttributes(ldap.isUseAllQueryAttributes());
                    dao.setOrder(ldap.getOrder());
                    LOGGER.debug("Adding LDAP attribute source for [{}]", ldap.getLdapUrl());
                    list.add(dao);
                });
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "LdapAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class LdapAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "ldapPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer ldapPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("ldapAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> ldapAttributeRepositories) {
            return plan -> {
                val results = ldapAttributeRepositories.toList()
                    .stream()
                    .filter(repo -> (Boolean) repo.getTags().get(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName()))
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };

        }
    }
}

