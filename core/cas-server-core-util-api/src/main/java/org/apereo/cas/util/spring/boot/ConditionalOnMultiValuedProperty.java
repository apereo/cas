package org.apereo.cas.util.spring.boot;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnMultiValuedProperty}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(MultiValuedPropertyCondition.class)
public @interface ConditionalOnMultiValuedProperty {
    /**
     * Name.
     *
     * @return the string
     */
    String name();

    /**
     * Value.
     *
     * @return the string [ ]
     */
    String[] value();
}
