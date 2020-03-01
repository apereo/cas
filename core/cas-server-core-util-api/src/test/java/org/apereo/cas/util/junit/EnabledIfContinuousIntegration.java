package org.apereo.cas.util.junit;

import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is {@link EnabledIfContinuousIntegration}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@Target({METHOD, TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@EnabledIf("#{ 'true'.equals(systemProperties['CI']) or 'true'.equals(systemEnvironment['CI']) }")
public @interface EnabledIfContinuousIntegration {
}
