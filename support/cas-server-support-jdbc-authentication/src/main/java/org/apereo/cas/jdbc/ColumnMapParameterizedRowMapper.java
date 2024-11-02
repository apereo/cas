package org.apereo.cas.jdbc;

import lombok.val;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Clone of {@link ColumnMapRowMapper}
 * that ignores nulls.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
public class ColumnMapParameterizedRowMapper extends ColumnMapRowMapper {
    @Override
    public final Map<String, Object> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
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
