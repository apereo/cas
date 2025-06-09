package org.apereo.cas.util.spring.beans;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertyResolver;
import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * This is {@link BeanCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface BeanCondition {
    /**
     * Always true bean condition.
     *
     * @return the bean condition
     */
    static BeanCondition alwaysTrue() {
        return on("cas.server.name").evenIfMissing();
    }

    /**
     * On bean condition.
     *
     * @param name the name
     * @return the bean condition
     */
    static BeanCondition on(final String name) {
        return new CompoundCondition(name);
    }
    
    /**
     * Copy bean condition into a new instance.
     *
     * @return bean condition
     */
    BeanCondition toStartWith();

    /**
     * Count conditions.
     *
     * @return the int
     */
    int count();

    /**
     * Match if missing bean condition.
     *
     * @return the bean condition
     */
    BeanCondition evenIfMissing();

    /**
     * With default value.
     *
     * @param value the value
     * @return the bean condition
     */
    BeanCondition withDefaultValue(String value);

    /**
     * Is the property value set to this value?
     *
     * @param value the value
     * @return the bean condition
     */
    BeanCondition havingValue(Serializable value);

    /**
     * Check of the value is a valid resource and exists.
     *
     * @return the bean condition
     */
    BeanCondition exists();

    /**
     * Is property value set to true.
     *
     * @return the bean condition
     */
    default BeanCondition isTrue() {
        return havingValue(Boolean.TRUE.toString());
    }

    /**
     * Is false bean condition.
     *
     * @return the bean condition
     */
    default BeanCondition isFalse() {
        return havingValue(Boolean.FALSE.toString());
    }

    /**
     * Is value defined as a valid URL?
     *
     * @return the bean condition
     */
    BeanCondition isUrl();

    /**
     * And another condition into the current chan.
     *
     * @param name the name
     * @return the bean condition
     */
    BeanCondition and(String name);

    /**
     * And bean condition.
     *
     * @param booleanSupplier the boolean supplier
     * @return the bean condition
     */
    BeanCondition and(Supplier<Boolean> booleanSupplier);

    /**
     * And bean conditions.
     *
     * @param condition the boolean supplier
     * @return the bean condition
     */
    BeanCondition and(Condition... condition);

    /**
     * And bean conditions.
     *
     * @param conditions the conditions
     * @return the bean condition
     */
    default BeanCondition and(final Collection<Condition> conditions) {
        conditions.forEach(this::and);
        return this;
    }

    /**
     * To supplier.
     *
     * @param applicationContext the application context
     * @return the supplier
     */
    Supplier<Boolean> given(PropertyResolver applicationContext);

    /**
     * Given application context, resolve.
     *
     * @param applicationContext the application context
     * @return the supplier
     */
    Supplier<Boolean> given(ApplicationContext applicationContext);

    interface Condition {}
}
