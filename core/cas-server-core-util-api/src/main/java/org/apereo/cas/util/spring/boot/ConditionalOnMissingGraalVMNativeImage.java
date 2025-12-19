package org.apereo.cas.util.spring.boot;

import module java.base;
import org.springframework.context.annotation.Conditional;

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
