package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.cas.jdbc.MultiRowJdbcPersonAttributeDao;
import org.apereo.cas.jdbc.QueryType;
import org.apereo.cas.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnClass(JpaBeans.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "CasPersonDirectoryJdbcConfiguration", proxyBeanMethods = false)
class CasPersonDirectoryJdbcConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.jdbc[0].sql");

    @Configuration(value = "JdbcAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "jdbcAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> jdbcAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val list = new ArrayList<PersonAttributeDao>();
                    val attrs = casProperties.getAuthn().getAttributeRepository();
                    attrs.getJdbc()
                        .stream()
                        .filter(jdbc -> StringUtils.isNotBlank(jdbc.getSql()) && StringUtils.isNotBlank(jdbc.getUrl()))
                        .forEach(jdbc -> {
                            val jdbcDao = createJdbcPersonAttributeDao(jdbc);
                            FunctionUtils.doIfNotNull(jdbc.getId(), id -> jdbcDao.setId(id));

                            val queryAttributes = CollectionUtils.wrap("username", jdbc.getUsername());
                            queryAttributes.putAll(jdbc.getQueryAttributes());
                            jdbcDao.setQueryAttributeMapping(queryAttributes);

                            val mapping = jdbc.getAttributes();
                            if (mapping != null && !mapping.isEmpty()) {
                                LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", jdbc.getUrl(), jdbc.getAttributes());
                                jdbcDao.setResultAttributeMapping(mapping);
                            }
                            jdbcDao.setRequireAllQueryAttributes(jdbc.isRequireAllAttributes());

                            val caseMode = CaseCanonicalizationMode.valueOf(jdbc.getCaseCanonicalization().toUpperCase(Locale.ENGLISH));
                            jdbcDao.setUsernameCaseCanonicalizationMode(caseMode);
                            jdbcDao.setDefaultCaseCanonicalizationMode(caseMode);
                            jdbcDao.setQueryType(QueryType.valueOf(jdbc.getQueryType().toUpperCase(Locale.ENGLISH)));
                            jdbcDao.setOrder(jdbc.getOrder());
                            jdbcDao.setEnabled(jdbc.getState() != AttributeRepositoryStates.DISABLED);
                            jdbcDao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                                jdbc.getState() == AttributeRepositoryStates.ACTIVE);
                            list.add(jdbcDao);
                        });
                    return BeanContainer.of(list);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }

        private static AbstractJdbcPersonAttributeDao configureJdbcPersonAttributeDao(
            final AbstractJdbcPersonAttributeDao dao, final JdbcPrincipalAttributesProperties jdbc) {

            val attributes = jdbc.getCaseInsensitiveQueryAttributes();
            val results = CollectionUtils.convertDirectedListToMap(attributes);

            dao.setCaseInsensitiveQueryAttributes(results
                .entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(),
                    StringUtils.isBlank(entry.getValue())
                        ? CaseCanonicalizationMode.valueOf(jdbc.getCaseCanonicalization().toUpperCase(Locale.ENGLISH))
                        : CaseCanonicalizationMode.valueOf(entry.getValue().toUpperCase(Locale.ENGLISH))))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
            return dao;
        }

        private static AbstractJdbcPersonAttributeDao createJdbcPersonAttributeDao(final JdbcPrincipalAttributesProperties jdbc) {
            val url = SpringExpressionLanguageValueResolver.getInstance().resolve(jdbc.getUrl());
            val sql = SpringExpressionLanguageValueResolver.getInstance().resolve(jdbc.getSql());
            if (jdbc.isSingleRow()) {
                LOGGER.debug("Configured single-row JDBC attribute repository for [{}]", url);
                val singleRow = new SingleRowJdbcPersonAttributeDao(JpaBeans.newDataSource(jdbc), sql);
                return configureJdbcPersonAttributeDao(singleRow, jdbc);
            }
            LOGGER.debug("Configured multi-row JDBC attribute repository for [{}]", url);
            val jdbcDao = new MultiRowJdbcPersonAttributeDao(JpaBeans.newDataSource(jdbc), sql);
            LOGGER.debug("Configured multi-row JDBC column mappings for [{}] are [{}]", url, jdbc.getColumnMappings());
            jdbcDao.setNameValueColumnMappings(jdbc.getColumnMappings());
            return configureJdbcPersonAttributeDao(jdbcDao, jdbc);
        }
    }

    @Configuration(value = "JdbcAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jdbcPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer jdbcPersonDirectoryAttributeRepositoryPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcAttributeRepositories")
            final BeanContainer<PersonAttributeDao> jdbcAttributeRepositories) {
            return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val results = jdbcAttributeRepositories.toList()
                        .stream()
                        .filter(PersonAttributeDao::isEnabled)
                        .collect(Collectors.toList());
                    plan.registerAttributeRepositories(results);
                })
                .otherwiseProxy()
                .get();
        }
    }
}
