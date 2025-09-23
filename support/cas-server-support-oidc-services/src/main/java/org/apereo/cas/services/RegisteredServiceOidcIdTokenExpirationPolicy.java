package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceOidcIdTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOidcIdTokenExpirationPolicy extends Serializable {
    /**
     * Time to kill for this ID token.
     *
     * @return time to kill.
     */
    String getTimeToKill();
}
