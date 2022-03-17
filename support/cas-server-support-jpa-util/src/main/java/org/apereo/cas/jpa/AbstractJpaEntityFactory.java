package org.apereo.cas.jpa;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link AbstractJpaEntityFactory}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 6.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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
    public T newInstance() {
        return FunctionUtils.doUnchecked(() -> getType().getDeclaredConstructor().newInstance());
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
