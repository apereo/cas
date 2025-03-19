package org.apereo.cas.util.spring.beans;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertyResolver;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This is {@link CompoundCondition}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@NoArgsConstructor
class CompoundCondition implements BeanCondition {
    private static final Pattern EXPRESSION_PATTERN = RegexUtils.createPattern("\\$\\{.+\\}");

    private final Deque<Condition> conditionList = new ArrayDeque<>();
    
    CompoundCondition(final String name) {
        and(name);
    }

    CompoundCondition(final Collection<Condition> conditions) {
        conditionList.addAll(conditions);
    }

    private static String resolvePropertyValue(final PropertyResolver propertyResolver, final PropertyCondition condition) {
        val spelResolver = SpringExpressionLanguageValueResolver.getInstance();
        try {
            val result = propertyResolver.getProperty(condition.getPropertyName(), condition.getDefaultValue());
            return spelResolver.resolve(result);
        } catch (final IllegalArgumentException e) {
            val placeholder = StringUtils.substringBetween(e.getMessage(), "\"", "\"");
            val matcher = EXPRESSION_PATTERN.matcher(placeholder);
            if (matcher.find()) {
                val match = matcher.group();
                val result = spelResolver.resolve(match);
                return StringUtils.isNotBlank(result)
                    ? placeholder.replaceAll(matcher.pattern().pattern(), result)
                    : placeholder;
            }
            return null;
        }
    }

    @Override
    public BeanCondition toStartWith() {
        return new CompoundCondition(conditionList);
    }

    @Override
    public int count() {
        return conditionList.size();
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition evenIfMissing() {
        if (conditionList.peek() instanceof final PropertyCondition condition) {
            condition.setMatchIfMissing(true);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition withDefaultValue(final String value) {
        if (conditionList.peek() instanceof final PropertyCondition condition) {
            condition.setDefaultValue(value);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition havingValue(final Serializable value) {
        if (conditionList.peek() instanceof final PropertyCondition condition) {
            condition.setHavingValue(value);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition exists() {
        if (conditionList.peek() instanceof final PropertyCondition condition) {
            condition.setExists(true);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition isUrl() {
        if (conditionList.peek() instanceof final PropertyCondition condition) {
            condition.setUrl(true);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition and(final String name) {
        conditionList.push(new PropertyCondition(name));
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition and(final Supplier<Boolean> booleanSupplier) {
        conditionList.push(new BooleanCondition(booleanSupplier.get()));
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public BeanCondition and(final Condition... condition) {
        Arrays.stream(condition).forEach(conditionList::push);
        return this;
    }

    @Override
    public Supplier<Boolean> given(final ApplicationContext applicationContext) {
        SpringExpressionLanguageValueResolver.getInstance().withApplicationContext(applicationContext);
        return given(applicationContext.getEnvironment());
    }

    @Override
    public Supplier<Boolean> given(final PropertyResolver propertyResolver) {
        return () -> conditionList
            .stream()
            .allMatch(cond -> {
                if (cond instanceof final PropertyCondition condition) {
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
                }
                if (cond instanceof BooleanCondition(var value)) {
                    return BooleanUtils.toBoolean(value);
                }
                return false;
            });
    }

    @SuppressWarnings("UnusedVariable")
    private record BooleanCondition(Boolean value) implements Condition {
    }

    @Data
    private static final class PropertyCondition implements Condition {
        private final String propertyName;

        private boolean matchIfMissing;

        private String defaultValue;

        private Serializable havingValue;

        private boolean exists;

        private boolean url;
    }

}
