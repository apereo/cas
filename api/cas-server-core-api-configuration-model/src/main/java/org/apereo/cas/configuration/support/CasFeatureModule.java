package org.apereo.cas.configuration.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.reflections.ReflectionUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;

/**
 * This is {@link CasFeatureModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasFeatureModule {

    /**
     * Is defined?
     *
     * @return true/false
     */
    @SneakyThrows
    @JsonIgnore
    default boolean isDefined() {
        val fields = ReflectionUtils.getAllFields(getClass(), field -> field.getAnnotation(RequiredProperty.class) != null);
        
        return fields
            .stream()
            .allMatch(Unchecked.predicate(field -> {
                var getter = getMethodName(field, "get");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    val value = method.invoke(this);
                    return value != null && StringUtils.isNotBlank(value.toString());
                }
                getter = getMethodName(field, "is");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    val value = method.invoke(this);
                    return value != null && BooleanUtils.toBoolean(value.toString());
                }
                return false;
            }));
    }

    /**
     * Is undefined ?.
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isUndefined() {
        return !isDefined();
    }

    private static String getMethodName(final Field field, final String prefix) {
        return prefix
            + field.getName().substring(0, 1).toUpperCase()
            + field.getName().substring(1);
    }
}
