package org.apereo.cas.configuration.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link RequiredProperty} that is put on top of a CAS property/field
 * to indicate the presence of the field is required for CAS to function correctly
 * and/or to recognize the existence of an enabled feature, etc.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RequiredProperty {
    /**
     * The message associated with this required property.
     * Might want to explain caveats and fallbacks to defaults.
     *
     * @return the msg
     */
    String message() default "The property is required";
}
