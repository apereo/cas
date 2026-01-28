package org.apereo.cas.jdbc;

import module java.base;
import org.apereo.cas.authentication.attribute.AbstractQueryPersonAttributeDao;
import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.util.RegexUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import module java.sql;

/**
 * Provides common logic for executing a JDBC based query including building the WHERE clause SQL string.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
public abstract class AbstractJdbcPersonAttributeDao<R> extends AbstractQueryPersonAttributeDao<PartialWhereClause> {
    private static final Pattern WHERE_PLACEHOLDER = RegexUtils.createPattern("\\{0\\}");

    private final JdbcTemplate simpleJdbcTemplate;
    private final String queryTemplate;
    @Getter
    @Setter
    private QueryType queryType = QueryType.AND;

    @Getter
    @Setter
    private Map<String, CaseCanonicalizationMode> caseInsensitiveDataAttributes;

    protected AbstractJdbcPersonAttributeDao(final DataSource ds, final String queryTemplate) {
        simpleJdbcTemplate = new JdbcTemplate(ds);
        this.queryTemplate = queryTemplate;
    }


    /**
     * Takes the {@link List} from the query and parses it into the {@link List} of {@link PersonAttributes} attributes to be returned.
     *
     * @param queryResults  Results from the query.
     * @param queryUserName The username passed in the query map, if no username attribute existed in the query Map null is provided.
     * @return The results of the query
     */
    protected abstract List<PersonAttributes> parseAttributeMapFromResults(List<R> queryResults, String queryUserName);

    protected abstract RowMapper<R> getRowMapper();

    @Override
    protected PartialWhereClause appendAttributeToQuery(final PartialWhereClause queryBuilder,
                                                        final String dataAttribute, final List<Object> queryValues) {
        val effectiveBuilder = queryBuilder != null ? queryBuilder : new PartialWhereClause();
        for (val queryValue : queryValues) {
            var queryString = queryValue != null ? queryValue.toString() : null;
            if (StringUtils.isNotBlank(queryString)) {
                if (!effectiveBuilder.getSql().isEmpty()) {
                    effectiveBuilder.getSql().append(' ').append(queryType.toString()).append(' ');
                }
                var queryValueMatcher = PersonAttributeDao.WILDCARD_PATTERN.matcher(queryString);
                var formattedQueryValue = queryValueMatcher.replaceAll("%");

                effectiveBuilder.getArguments().add(formattedQueryValue);
                if (dataAttribute != null) {
                    effectiveBuilder.getSql().append(dataAttribute);
                    if (formattedQueryValue.equals(queryString)) {
                        effectiveBuilder.getSql().append(" = ");
                    } else {
                        effectiveBuilder.getSql().append(" LIKE ");
                    }
                }
                effectiveBuilder.getSql().append('?');
            }
        }
        return effectiveBuilder;
    }

    @Override
    protected List<PersonAttributes> getPeopleForQuery(final PartialWhereClause queryBuilder, final String queryUserName) {
        val rowMapper = getRowMapper();
        if (queryBuilder != null) {
            val partialSqlWhere = queryBuilder.getSql();
            val queryMatcher = WHERE_PLACEHOLDER.matcher(queryTemplate);
            val querySQL = queryMatcher.replaceAll(partialSqlWhere.toString());
            LOGGER.debug("Executing [{}] with arguments [{}]", queryTemplate, queryBuilder.getArguments());
            val results = simpleJdbcTemplate.query(querySQL, rowMapper, queryBuilder.getArguments().toArray());
            LOGGER.debug("Executed [{}] with arguments [{}] and got results [{}]", queryTemplate, queryBuilder.getSql(), results);
            return parseAttributeMapFromResults(results, queryUserName);
        }
        LOGGER.debug("Executing [{}]", queryTemplate);
        val results = simpleJdbcTemplate.query(queryTemplate, rowMapper);
        LOGGER.debug("Executed [{}] and got results [{}]", queryTemplate, results);
        return parseAttributeMapFromResults(results, queryUserName);
    }
}
