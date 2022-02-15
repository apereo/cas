package org.apereo.cas.util.spring.beans;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.PropertyResolver;

import java.util.function.Supplier;

/**
 * This is {@link BeanCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface BeanCondition {

    /**
     * On bean condition.
     *
     * @param name the name
     * @return the bean condition
     */
    static BeanCondition onProperty(final String name) {
        return new PropertyBeanCondition(name);
    }

    /**
     * Match if missing bean condition.
     *
     * @return the bean condition
     */
    BeanCondition matchIfMissing();

    /**
     * With default value.
     *
     * @param value the value
     * @return the bean condition
     */
    BeanCondition withDefaultValue(String value);

    /**
     * To supplier supplier.
     *
     * @param applicationContext the application context
     * @return the supplier
     */
    Supplier<Boolean> matches(final PropertyResolver applicationContext);

    @RequiredArgsConstructor
    class PropertyBeanCondition implements BeanCondition {
        private final String propertyName;

        private boolean matchIfMissing;

        private String defaultValue;

        @Override
        public BeanCondition matchIfMissing() {
            this.matchIfMissing = true;
            return this;
        }

        @Override
        public BeanCondition withDefaultValue(final String value) {
            this.defaultValue = value;
            return this;
        }

        @Override
        public Supplier<Boolean> matches(final PropertyResolver propertyResolver) {
            return () -> {
                if (matchIfMissing && !propertyResolver.containsProperty(this.propertyName)) {
                    return true;
                }
                val result = propertyResolver.getProperty(propertyName, defaultValue);
                return StringUtils.isNotBlank(result);
            };
        }
    }
}
