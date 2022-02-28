package org.apereo.cas.util.spring.beans;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.PropertyResolver;

import java.io.Serializable;
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
    static BeanCondition on(final String name) {
        return new PropertyBeanCondition(name);
    }

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
        return havingValue("true");
    }

    /**
     * Is value defined as a valid URL?
     *
     * @return the bean condition
     */
    BeanCondition isUrl();

    /**
     * To supplier supplier.
     *
     * @param applicationContext the application context
     * @return the supplier
     */
    Supplier<Boolean> given(PropertyResolver applicationContext);

    @RequiredArgsConstructor
    class PropertyBeanCondition implements BeanCondition {
        private final String propertyName;

        private boolean matchIfMissing;

        private String defaultValue;

        private Serializable havingValue;

        private boolean exists;

        private boolean url;

        @Override
        public BeanCondition evenIfMissing() {
            this.matchIfMissing = true;
            return this;
        }

        @Override
        public BeanCondition withDefaultValue(final String value) {
            this.defaultValue = value;
            return this;
        }

        @Override
        public BeanCondition havingValue(final Serializable value) {
            this.havingValue = value;
            return this;
        }

        @Override
        public BeanCondition exists() {
            this.exists = true;
            return this;
        }

        @Override
        public BeanCondition isUrl() {
            this.url = true;
            return this;
        }

        @Override
        public Supplier<Boolean> given(final PropertyResolver propertyResolver) {
            return () -> {
                if (matchIfMissing && !propertyResolver.containsProperty(this.propertyName)) {
                    return true;
                }
                val result = propertyResolver.getProperty(propertyName, defaultValue);
                if (havingValue != null) {
                    return havingValue.toString().equalsIgnoreCase(result);
                }
                if (url && StringUtils.isNotBlank(result)) {
                    return RegexUtils.find("^https*:\\/\\/.+", result);
                }
                if (exists) {
                    return ResourceUtils.doesResourceExist(result);
                }
                return StringUtils.isNotBlank(result);
            };
        }
    }
}
