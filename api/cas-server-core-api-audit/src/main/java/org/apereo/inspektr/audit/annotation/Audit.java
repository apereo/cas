package org.apereo.inspektr.audit.annotation;

import module java.base;
import org.apache.commons.lang3.StringUtils;

/**
 * States that this method should be logged for auditing purposes.
 * 
 * @author Alice Leung
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audit {

    /**
     * The action to write to the log when we audit this method.  Value must be defined.
     * @return the action to write to the logs.
     */
    String action();

    /**
     * Reference name of the resource resolver to use.
     *
     * @return the reference to the resource resolver.  CANNOT be NULL.
     */
    String resourceResolverName();

    /**
     * Reference name of the action resolver to use.
     *
     * @return the reference to the action resolver.  CANNOT be NULL.
     */
    String actionResolverName();

    /**
     * Reference name of the principal resolver to use.
     *
     * @return the reference to the principal resolver.
     */
    String principalResolverName() default StringUtils.EMPTY;
}
