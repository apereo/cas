package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Expiration policy that dictates how long should this service be kept alive.
 *
 * @author Misagh Moayyed
 * @since 5.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceExpirationPolicy extends Serializable {

    /**
     * Gets expiration date that indicates when this may be expired.
     *
     * @return the expiration date
     */
    String getExpirationDate();

    /**
     * Notify service owners and contacts
     * when this service is marked as expired and is about to be deleted.
     *
     * @return true/false
     */
    boolean isNotifyWhenDeleted();

    /**
     * Notify service owners and contacts
     * when this service is marked as expired.
     *
     * @return true/false
     */
    boolean isNotifyWhenExpired();

    /**
     * Whether service should be deleted from the registry
     * if and when expired.
     *
     * @return true/false
     */
    boolean isDeleteWhenExpired();

    @JsonIgnore
    boolean isExpired();
}
