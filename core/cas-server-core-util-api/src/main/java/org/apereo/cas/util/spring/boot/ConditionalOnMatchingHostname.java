package org.apereo.cas.util.spring.boot;

import org.springframework.context.annotation.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnMatchingHostname} allows beans to be created on one host in a cluster.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(MatchingHostnameCondition.class)
public @interface ConditionalOnMatchingHostname {
    /**
     * Name of the property containing the hostname to
     * match as its value (may be a Java regular expression).
     *
     * @return the pattern or the host name.
     */
    String name();

}
