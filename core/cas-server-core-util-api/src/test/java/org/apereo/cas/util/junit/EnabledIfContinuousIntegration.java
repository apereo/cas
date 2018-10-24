package org.apereo.cas.util.junit;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This is {@link EnabledIfContinuousIntegration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
public @interface EnabledIfContinuousIntegration {
}
