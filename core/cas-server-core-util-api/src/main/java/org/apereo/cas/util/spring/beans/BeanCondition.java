package org.apereo.cas.util.spring.beans;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.PropertyResolver;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
     * And another condition into the current chan.
     *
     * @param name the name
     * @return the bean condition
     */
    BeanCondition and(String name);

    /**
     * To supplier supplier.
     *
     * @param applicationContext the application context
     * @return the supplier
     */
    Supplier<Boolean> given(PropertyResolver applicationContext);

    @Data
    class Condition {
        private final String propertyName;

        private boolean matchIfMissing;

        private String defaultValue;

        private Serializable havingValue;

        private boolean exists;

        private boolean url;
    }

    @RequiredArgsConstructor
    class PropertyBeanCondition implements BeanCondition {
        private static final Pattern EXPRESSION_PATTERN = RegexUtils.createPattern("\\$\\{.+\\}");

        private final Deque<Condition> conditionList = new ArrayDeque<>();

        PropertyBeanCondition(final String name) {
            conditionList.push(new Condition(name));
        }

        private static String resolvePropertyValue(final PropertyResolver propertyResolver, final Condition condition) {
            try {
                val result = propertyResolver.getProperty(condition.getPropertyName(), condition.getDefaultValue());
                return SpringExpressionLanguageValueResolver.getInstance().resolve(result);
            } catch (final IllegalArgumentException e) {
                var placeholder = StringUtils.substringBetween(e.getMessage(), "\"", "\"");
                val matcher = EXPRESSION_PATTERN.matcher(placeholder);
                if (matcher.find()) {
                    val match = matcher.group();
                    val result = SpringExpressionLanguageValueResolver.getInstance().resolve(match);
                    return placeholder.replaceAll(matcher.pattern().pattern(), result);
                }
                return null;
            }
        }

        @Override
        public BeanCondition evenIfMissing() {
            conditionList.peek().setMatchIfMissing(true);
            return this;
        }

        @Override
        public BeanCondition withDefaultValue(final String value) {
            conditionList.peek().setDefaultValue(value);
            return this;
        }

        @Override
        public BeanCondition havingValue(final Serializable value) {
            conditionList.peek().setHavingValue(value);
            return this;
        }

        @Override
        public BeanCondition exists() {
            conditionList.peek().setExists(true);
            return this;
        }

        @Override
        public BeanCondition isUrl() {
            conditionList.peek().setUrl(true);
            return this;
        }

        @Override
        public BeanCondition and(final String name) {
            conditionList.push(new Condition(name));
            return this;
        }

        @Override
        public Supplier<Boolean> given(final PropertyResolver propertyResolver) {
            return () -> conditionList.stream().allMatch(condition -> {
                if (condition.isMatchIfMissing() && !propertyResolver.containsProperty(condition.getPropertyName())) {
                    return true;
                }
                val result = resolvePropertyValue(propertyResolver, condition);
                if (condition.getHavingValue() != null) {
                    return condition.getHavingValue().toString().equalsIgnoreCase(result);
                }
                if (condition.isUrl() && StringUtils.isNotBlank(result)) {
                    return RegexUtils.find("^https*:\\/\\/.+", result);
                }
                if (condition.isExists()) {
                    return ResourceUtils.doesResourceExist(result);
                }
                return StringUtils.isNotBlank(result);
            });
        }
    }
}
