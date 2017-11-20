package org.apereo.cas.configuration.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link RequiresModule} that is put on top of a CAS properties class
 * to indicate the required/using module that takes advantage of the settings.
 * The module typically needs to be available on the classpath at runtime
 * in order to activate a certain feature in CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresModule {
    /**
     * Indicate the name of the module required.
     * Module names typically don't carry prefixes such as {@code cas-server-}.
     * The name indicates only the actual functionality.
     *
     * @return the name
     */
    String name();

    /**
     * Indicates the module is automatically included and is present
     * on the classpath. In such cases, the feature at hand may only be tweaked
     * using a toggle in settings.
     *
     * @return the boolean
     */
    boolean automated() default false;
}
