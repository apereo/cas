package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.RowMapper;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An {@link PersonAttributeDao}
 * implementation that maps attribute names and values from name and value column
 * pairs.
 * This class expects 1 to N row results for a query, with each row containing 1 to N name
 * value attribute mappings and the userName of the user the attributes are for. This contrasts
 * {@link SingleRowJdbcPersonAttributeDao} which expects
 * a single row result for a user query.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
@Getter
public class MultiRowJdbcPersonAttributeDao extends AbstractJdbcPersonAttributeDao<Map<String, Object>> {
    private static final RowMapper<Map<String, Object>> MAPPER = new ColumnMapParameterizedRowMapper();

    /**
     * {@link Map} of columns from a name column to value columns.
     * Keys are Strings, Values are Strings or Set of Strings.
     */
    private Map<String, Set<String>> nameValueColumnMappings;

    public MultiRowJdbcPersonAttributeDao(final DataSource ds, final String sql) {
        super(ds, sql);
    }

    public void setNameValueColumnMappings(final Map<String, ?> nameValueColumnMap) {
        this.nameValueColumnMappings = parseAttributeToAttributeMapping(nameValueColumnMap);
    }

    @Override
    protected RowMapper<Map<String, Object>> getRowMapper() {
        return MAPPER;
    }

    @Override
    protected List<PersonAttributes> parseAttributeMapFromResults(final List<Map<String, Object>> queryResults, final String queryUserName) {
        val peopleAttributesBuilder = new HashMap<String, Map<String, List<Object>>>();
        val userNameAttribute = getConfiguredUserNameAttribute();

        for (val queryResult : queryResults) {
            val userName = getUserName(queryUserName, queryResult, userNameAttribute);
            val attributes = peopleAttributesBuilder.computeIfAbsent(userName, __ -> new LinkedHashMap<>());
            for (val columnMapping : nameValueColumnMappings.entrySet()) {
                val keyColumn = columnMapping.getKey();
                val attrNameObj = queryResult.get(keyColumn);
                LOGGER.debug("Collecting attribute name=[{}] from column=[{}]", attrNameObj, keyColumn);
                if (attrNameObj == null && !queryResult.containsKey(keyColumn)) {
                    throw new IllegalArgumentException("No attribute key column named " + keyColumn + " exists in result set");
                }
                val attrName = String.valueOf(attrNameObj);

                val valueColumns = columnMapping.getValue();
                val attrValues = new ArrayList<>(valueColumns.size());
                for (val valueColumn : valueColumns) {
                    val attrValue = queryResult.get(valueColumn);
                    LOGGER.debug("Collecting attribute value=[{}] from column=[{}]", attrValue, valueColumn);
                    if (attrValue == null && !queryResult.containsKey(valueColumn)) {
                        throw new IllegalArgumentException("No attribute value column named " + valueColumn + " exists in result set");
                    }
                    attrValues.add(attrValue);
                }
                addResult(attributes, attrName, attrValues);
            }
        }

        val people = new ArrayList<PersonAttributes>(peopleAttributesBuilder.size());
        for (val mappedAttributesEntry : peopleAttributesBuilder.entrySet()) {
            val userName = mappedAttributesEntry.getKey();
            val attributes = mappedAttributesEntry.getValue();
            val person = new SimplePersonAttributes(userName, attributes);
            LOGGER.debug("Collecting person=[{}]", person);
            people.add(person);
        }
        return people;
    }

    private String getUserName(final String queryUserName, final Map<String, Object> queryResult, final String userNameAttribute) {
        if (this.isUserNameAttributeConfigured() && queryResult.containsKey(userNameAttribute)) {
            return queryResult.get(userNameAttribute).toString();
        }
        if (queryUserName != null) {
            return queryUserName;
        }
        if (queryResult.containsKey(userNameAttribute)) {
            return queryResult.get(userNameAttribute).toString();
        }
        throw new IllegalArgumentException("No username column named " + userNameAttribute + " exists in result set");
    }

    private static <K, V> void addResult(final Map<K, List<V>> results, final K key, final Object value) {
        if (value == null) {
            return;
        }
        val currentValue = results.computeIfAbsent(key, __ -> new ArrayList<>());
        if (value instanceof List) {
            currentValue.addAll((Collection<? extends V>) value);
        } else {
            currentValue.add((V) value);
        }
    }
}
