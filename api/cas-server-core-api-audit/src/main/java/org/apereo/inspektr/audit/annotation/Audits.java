package org.apereo.inspektr.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * States that this method should be logged for auditing purposes.
 * This is a holder annotation to hold multiple {@link Audit} annotations.
 * 
 * @author Alice Leung
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audits {

    /**
     * audit value.
     *
     * @return the audit [ ]
     */
    Audit[] value();
}
