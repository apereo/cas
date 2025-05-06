package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is {@link JdbcPersonAttributeUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@UtilityClass
@Slf4j
public class JdbcPersonAttributeUtils {
    /**
     * New jdbc attribute repository dao list.
     *
     * @param properties the properties
     * @return the list
     */
    public static List<? extends PersonAttributeDao> newJdbcAttributeRepositoryDao(final List<JdbcPrincipalAttributesProperties> properties) {
        return properties
            .stream()
            .filter(jdbc -> StringUtils.isNotBlank(jdbc.getSql()) && StringUtils.isNotBlank(jdbc.getUrl()))
            .map(jdbc -> {
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
                jdbcDao.putTag("state", jdbc.getState());
                return jdbcDao;
            })
            .toList();
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
}
