package org.apereo.cas.util.junit;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This is {@link DisabledIfContinuousIntegration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public @interface DisabledIfContinuousIntegration {
}
