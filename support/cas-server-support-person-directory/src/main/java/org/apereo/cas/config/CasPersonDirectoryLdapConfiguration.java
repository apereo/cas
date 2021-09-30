package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;

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

    @ConditionalOnMissingBean(name = "ldapAttributeRepositories")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public List<IPersonAttributeDao> ldapAttributeRepositories(final CasConfigurationProperties casProperties) {
        val list = new ArrayList<IPersonAttributeDao>();
        val attrs = casProperties.getAuthn().getAttributeRepository();
        attrs.getLdap()
            .stream()
            .filter(ldap -> StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getLdapUrl()))
            .forEach(ldap -> {
                val ldapDao = new LdaptivePersonAttributeDao();
                FunctionUtils.doIfNotNull(ldap.getId(), ldapDao::setId);
                LOGGER.debug("Configured LDAP attribute source for [{}] and baseDn [{}]", ldap.getLdapUrl(), ldap.getBaseDn());
                ldapDao.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
                ldapDao.setBaseDN(ldap.getBaseDn());

                LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(), ldap.getSearchFilter());
                ldapDao.setSearchFilter(ldap.getSearchFilter());

                val constraints = new SearchControls();
                if (ldap.getAttributes() != null && !ldap.getAttributes().isEmpty()) {
                    LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", ldap.getLdapUrl(), ldap.getAttributes());
                    ldapDao.setResultAttributeMapping(ldap.getAttributes());
                    val attributes = (String[]) ldap.getAttributes().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
                    constraints.setReturningAttributes(attributes);
                } else {
                    LOGGER.debug("Retrieving all attributes as no explicit attribute mappings are defined for [{}]", ldap.getLdapUrl());
                    constraints.setReturningAttributes(null);
                }

                val binaryAttributes = ldap.getBinaryAttributes();
                if (binaryAttributes != null && !binaryAttributes.isEmpty()) {
                    LOGGER.debug("Setting binary attributes [{}]", binaryAttributes);
                    ldapDao.setBinaryAttributes(binaryAttributes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                }

                val searchEntryHandlers = ldap.getSearchEntryHandlers();
                if (searchEntryHandlers != null && !searchEntryHandlers.isEmpty()) {
                    val entryHandlers = LdapUtils.newLdaptiveEntryHandlers(searchEntryHandlers);
                    if (!entryHandlers.isEmpty()) {
                        LOGGER.debug("Setting entry handlers [{}]", entryHandlers);
                        ldapDao.setEntryHandlers(entryHandlers.toArray(new LdapEntryHandler[0]));
                    }
                    val searchResultHandlers = LdapUtils.newLdaptiveSearchResultHandlers(searchEntryHandlers);
                    if (!searchResultHandlers.isEmpty()) {
                        LOGGER.debug("Setting search result handlers [{}]", searchResultHandlers);
                        ldapDao.setSearchResultHandlers(searchResultHandlers.toArray(new SearchResultHandler[0]));
                    }
                }

                if (ldap.isSubtreeSearch()) {
                    LOGGER.debug("Configured subtree searching for [{}]", ldap.getLdapUrl());
                    constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                }
                constraints.setDerefLinkFlag(true);
                ldapDao.setSearchControls(constraints);
                ldapDao.setOrder(ldap.getOrder());
                LOGGER.debug("Adding LDAP attribute source for [{}]", ldap.getLdapUrl());
                list.add(ldapDao);
            });

        return list;
    }

    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer ldapPersonDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("ldapAttributeRepositories") final ObjectProvider<List<IPersonAttributeDao>> ldapAttributeRepositories) {
        return plan -> plan.registerAttributeRepositories(ldapAttributeRepositories.getObject());
    }
}

