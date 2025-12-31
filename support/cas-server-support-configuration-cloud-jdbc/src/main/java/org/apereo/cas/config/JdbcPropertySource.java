package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link JdbcPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@SuppressWarnings("NullAway.Init")
public class JdbcPropertySource extends EnumerablePropertySource<JdbcTemplate> implements MutablePropertySource<JdbcTemplate> {
    private Set<String> propertyNames;

    public JdbcPropertySource(final String context, final JdbcTemplate jdbcTemplate) {
        super(context, jdbcTemplate);
        refresh();
    }

    @Override
    public void refresh() {
        this.propertyNames = new HashSet<>(getSource().queryForList("SELECT name FROM CAS_SETTINGS_TABLE", String.class));
    }

    @Override
    public void removeAll() {
        getSource().update("DELETE FROM CAS_SETTINGS_TABLE");
        propertyNames.clear();
    }

    @Override
    public void removeProperty(final String name) {
        getSource().update("DELETE FROM CAS_SETTINGS_TABLE WHERE name = ?", name);
        propertyNames.remove(name);
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val updated = getSource().update("UPDATE CAS_SETTINGS_TABLE SET value = ? WHERE name = ?", value, name);
        if (updated == 0) {
            getSource().update("INSERT INTO CAS_SETTINGS_TABLE(name, value) VALUES(?, ?)", name, value);
        }
        propertyNames.add(name);
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (propertyNames.contains(name)) {
            try {
                return getSource().queryForObject("SELECT value FROM CAS_SETTINGS_TABLE WHERE name = ?", Object.class, name);
            } catch (final EmptyResultDataAccessException e) {
                LOGGER.trace("No value could be found in the database for property name [{}]", name);
            }
        }
        return null;
    }
}

