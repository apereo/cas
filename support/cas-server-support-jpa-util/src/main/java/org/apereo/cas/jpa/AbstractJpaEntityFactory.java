package org.apereo.cas.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This is {@link AbstractJpaEntityFactory}.
 *
 * @param <T> the type parameter
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractJpaEntityFactory<T> {
    private final String dialect;

    /**
     * Gets type.
     *
     * @return the type
     */
    public abstract Class<T> getType();

    /**
     * New document.
     *
     * @return the document
     */
    @SneakyThrows
    public T newInstance() {
        return (T) getType().getDeclaredConstructor().newInstance();
    }

    /**
     * Is oracle ?.
     *
     * @return true/false
     */
    protected boolean isOracle() {
        return this.dialect.contains("Oracle");
    }

    /**
     * Is my sql ?.
     *
     * @return true/false
     */
    protected boolean isMySql() {
        return this.dialect.contains("MySQL");
    }

    /**
     * Is postgres ?.
     *
     * @return true/false
     */
    protected boolean isPostgres() {
        return this.dialect.contains("PostgreSQL");
    }

    /**
     * Is maria db ?
     *
     * @return true/false
     */
    protected boolean isMariaDb() {
        return this.dialect.contains("MariaDB");
    }

}
