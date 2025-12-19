package org.apereo.cas.jpa;
import module java.base;

/**
 * This is {@link JpaEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface JpaEntityFactory<T> {
    /**
     * Gets type.
     *
     * @return the type
     */
    Class<T> getType();

    /**
     * Gets dialect.
     *
     * @return dialect
     */
    String getDialect();

    /**
     * Is MS SQL Server ?.
     *
     * @return true/false
     */
    default boolean isMsSqlServer() {
        return getDialect().contains("SQLServer");
    }

    /**
     * Is oracle ?.
     *
     * @return true/false
     */
    default boolean isOracle() {
        return getDialect().contains("Oracle");
    }

    /**
     * Is mysql ?.
     *
     * @return true/false
     */
    default boolean isMySql() {
        return getDialect().contains("MySQL");
    }

    /**
     * Is postgres ?.
     *
     * @return true/false
     */
    default boolean isPostgres() {
        return getDialect().contains("PostgreSQL");
    }

    /**
     * Is maria db ?
     *
     * @return true/false
     */
    default boolean isMariaDb() {
        return getDialect().contains("MariaDB");
    }
}
