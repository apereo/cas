package org.apereo.cas.jdbc;

import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.util.LoggingUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link DatabasePasswordEncoderUtils}.
 *
 * @author Yusuf Gunduz
 *
 * @since 7.3.0
 *
 */
@Slf4j
@UtilityClass
public class DatabasePasswordEncoderUtils {

    /**
     * New database password encoder.
     *
     * @param properties         the properties
     * @return the database password encoder
     */
    public static DatabasePasswordEncoder newDatabasePasswordEncoder(
        final QueryEncodeJdbcAuthenticationProperties properties) {
        val type = properties.getDatabasePasswordEncoder();

        if (StringUtils.isBlank(type)) {
            LOGGER.trace("No database password encoder type is defined, and so a QueryAndEncodeDatabasePasswordEncoder shall be created");
            return getDefaultPasswordEncoder(properties);
        }

        if (type.contains(".")) {
            try {
                LOGGER.debug("Configuration indicates use of a custom database password encoder [{}]", type);
                val clazz = (Class<DatabasePasswordEncoder>) Class.forName(type);
                return clazz.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                val msg = "Falling back to a QueryAndEncodeDatabasePasswordEncoder database password encoder as CAS has failed to create "
                          + "an instance of the custom database password encoder class " + type;
                LoggingUtils.error(LOGGER, msg, e);
                return getDefaultPasswordEncoder(properties);
            }
        }
        LOGGER.trace("No database password encoder shall be created given the requested encoder type [{}]", type);
        return getDefaultPasswordEncoder(properties);
    }

    /**
     * Get default password encoder database password encoder.
     * @param properties         the properties
     * @return the default database password encoder
     */
    private static DatabasePasswordEncoder getDefaultPasswordEncoder(final QueryEncodeJdbcAuthenticationProperties properties) {
        return new QueryAndEncodeDatabasePasswordEncoder(properties);
    }
}
