package org.apereo.cas.jdbc;

import module java.base;
import module java.sql;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Clone of {@link ColumnMapRowMapper}
 * that ignores nulls.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
public class ColumnMapParameterizedRowMapper extends ColumnMapRowMapper {
    @Override
    public final @NonNull Map<String, Object> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        val rsmd = rs.getMetaData();
        val columnCount = rsmd.getColumnCount();
        val mapOfColValues = createColumnMap(columnCount);

        for (var i = 1; i <= columnCount; i++) {
            val columnName = JdbcUtils.lookupColumnName(rsmd, i);
            val obj = getColumnValue(rs, i);
            val key = getColumnKey(columnName);
            mapOfColValues.put(key, obj);
        }
        return mapOfColValues;
    }
}
