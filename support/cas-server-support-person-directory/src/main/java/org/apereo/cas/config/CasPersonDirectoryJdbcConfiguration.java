package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.QueryType;
import org.apereo.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.MultiRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnClass(value = JpaBeans.class)
@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.jdbc[0]", value = "sql")
@Configuration(value = "CasPersonDirectoryJdbcConfiguration", proxyBeanMethods = false)
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryJdbcConfiguration {
    private static AbstractJdbcPersonAttributeDao configureJdbcPersonAttributeDao(
        final AbstractJdbcPersonAttributeDao dao, final JdbcPrincipalAttributesProperties jdbc) {

        val attributes = jdbc.getCaseInsensitiveQueryAttributes();
        val results = CollectionUtils.convertDirectedListToMap(attributes);

        dao.setCaseInsensitiveQueryAttributes(results
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(),
                StringUtils.isBlank(entry.getValue())
                    ? CaseCanonicalizationMode.valueOf(jdbc.getCaseCanonicalization().toUpperCase())
                    : CaseCanonicalizationMode.valueOf(entry.getValue().toUpperCase())))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
        return dao;
    }

    private static AbstractJdbcPersonAttributeDao createJdbcPersonAttributeDao(final JdbcPrincipalAttributesProperties jdbc) {
        val url = SpringExpressionLanguageValueResolver.getInstance().resolve(jdbc.getUrl());
        if (jdbc.isSingleRow()) {
            LOGGER.debug("Configured single-row JDBC attribute repository for [{}]", url);
            return configureJdbcPersonAttributeDao(
                new SingleRowJdbcPersonAttributeDao(
                    JpaBeans.newDataSource(jdbc),
                    jdbc.getSql()
                ), jdbc);
        }
        LOGGER.debug("Configured multi-row JDBC attribute repository for [{}]", url);
        val jdbcDao = new MultiRowJdbcPersonAttributeDao(
            JpaBeans.newDataSource(jdbc),
            jdbc.getSql()
        );
        LOGGER.debug("Configured multi-row JDBC column mappings for [{}] are [{}]", url, jdbc.getColumnMappings());
        jdbcDao.setNameValueColumnMappings(jdbc.getColumnMappings());
        return configureJdbcPersonAttributeDao(jdbcDao, jdbc);
    }

    @ConditionalOnMissingBean(name = "jdbcAttributeRepositories")
    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public List<IPersonAttributeDao> jdbcAttributeRepositories(final CasConfigurationProperties casProperties) {
        val list = new ArrayList<IPersonAttributeDao>();
        val attrs = casProperties.getAuthn().getAttributeRepository();
        attrs.getJdbc()
            .stream()
            .filter(jdbc -> StringUtils.isNotBlank(jdbc.getSql()) && StringUtils.isNotBlank(jdbc.getUrl()))
            .forEach(jdbc -> {
                val jdbcDao = createJdbcPersonAttributeDao(jdbc);
                FunctionUtils.doIfNotNull(jdbc.getId(), jdbcDao::setId);

                val queryAttributes = CollectionUtils.wrap("username", jdbc.getUsername());
                queryAttributes.putAll(jdbc.getQueryAttributes());
                jdbcDao.setQueryAttributeMapping(queryAttributes);

                val mapping = jdbc.getAttributes();
                if (mapping != null && !mapping.isEmpty()) {
                    LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", jdbc.getUrl(), jdbc.getAttributes());
                    jdbcDao.setResultAttributeMapping(mapping);
                }
                jdbcDao.setRequireAllQueryAttributes(jdbc.isRequireAllAttributes());

                val caseMode = CaseCanonicalizationMode.valueOf(jdbc.getCaseCanonicalization().toUpperCase());
                jdbcDao.setUsernameCaseCanonicalizationMode(caseMode);
                jdbcDao.setDefaultCaseCanonicalizationMode(caseMode);
                jdbcDao.setQueryType(QueryType.valueOf(jdbc.getQueryType().toUpperCase()));
                jdbcDao.setOrder(jdbc.getOrder());
                list.add(jdbcDao);
            });
        return list;
    }

    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer jdbcPersonDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("jdbcAttributeRepositories") final ObjectProvider<List<IPersonAttributeDao>> jdbcAttributeRepositories) {
        return plan -> plan.registerAttributeRepositories(jdbcAttributeRepositories.getObject());
    }
}
