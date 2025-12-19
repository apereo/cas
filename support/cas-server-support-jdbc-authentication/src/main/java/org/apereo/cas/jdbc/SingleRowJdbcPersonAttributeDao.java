package org.apereo.cas.jdbc;

import module java.base;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.val;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import module java.sql;

/**
 * An {@link PersonAttributeDao}
 * implementation that maps from column names in the result of a SQL query
 * to attribute names. <br>
 * You must set a Map from column names to attribute names and only column names
 * appearing as keys in that map will be used.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @since 7.1.0
 */
public class SingleRowJdbcPersonAttributeDao extends AbstractJdbcPersonAttributeDao<Map<String, Object>> {
    private static final RowMapper<Map<String, Object>> MAPPER = new ColumnMapRowMapper();

    public SingleRowJdbcPersonAttributeDao(final DataSource ds, final String sql) {
        super(ds, sql);
    }

    @Override
    protected RowMapper<Map<String, Object>> getRowMapper() {
        return MAPPER;
    }

    @Override
    protected List<PersonAttributes> parseAttributeMapFromResults(final List<Map<String, Object>> queryResults, final String queryUserName) {
        val peopleAttributes = new ArrayList<PersonAttributes>(queryResults.size());
        for (val queryResult : queryResults) {
            val multivaluedQueryResult = toMultivaluedMap(queryResult);
            val userNameAttribute = getConfiguredUserNameAttribute();
            if (isUserNameAttributeConfigured() && queryResult.containsKey(userNameAttribute)) {
                peopleAttributes.add(SimplePersonAttributes.fromAttribute(userNameAttribute, multivaluedQueryResult));
            } else if (queryUserName != null) {
                peopleAttributes.add(new SimplePersonAttributes(queryUserName, multivaluedQueryResult));
            } else {
                peopleAttributes.add(SimplePersonAttributes.fromAttribute(userNameAttribute, multivaluedQueryResult));
            }
        }
        return peopleAttributes;
    }
}
