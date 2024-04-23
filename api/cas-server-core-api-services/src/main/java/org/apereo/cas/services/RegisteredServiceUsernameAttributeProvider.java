package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Strategy interface to define what username attribute should
 * be returned for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceUsernameAttributeProvider extends Serializable {
    /**
     * Resolve the username that is to be returned to CAS clients.
     *
     * @param context the context
     * @return the username value configured for this service
     * @throws Throwable the throwable
     */
    String resolveUsername(RegisteredServiceUsernameProviderContext context) throws Throwable;
}
