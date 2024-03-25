package org.apereo.cas.persondir.cache;

import org.aopalliance.intercept.MethodInvocation;
import java.io.Serializable;

/**
 * Generates a unique key based on the description of an invocation to an
 * intercepted method.
 *
 * @author Alex Ruiz
 * @since 7.1.0
 */
public interface CacheKeyGenerator {

    /**
     * Generates the key for a cache entry.
     *
     * @param methodInvocation the description of an invocation to the intercepted method.
     * @return the created key.
     */
    Serializable generateKey(MethodInvocation methodInvocation);

    /**
     * Sets default attribute name.
     *
     * @param usernameAttribute the username attribute
     */
    void setDefaultAttributeName(String usernameAttribute);

}
