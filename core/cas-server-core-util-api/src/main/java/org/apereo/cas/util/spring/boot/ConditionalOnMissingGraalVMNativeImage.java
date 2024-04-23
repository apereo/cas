package org.apereo.cas.util.spring.boot;

import org.springframework.context.annotation.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnMissingGraalVMNativeImage}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(CasGraalVMNativeImageDisabledCondition.class)
public @interface ConditionalOnMissingGraalVMNativeImage {
}
