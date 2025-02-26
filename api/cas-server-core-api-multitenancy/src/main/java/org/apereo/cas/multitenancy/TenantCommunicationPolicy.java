package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * This is {@link TenantCommunicationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantCommunicationPolicy extends Serializable {

    /**
     * Gets email communication policy.
     *
     * @return the email communication policy
     */
    TenantEmailCommunicationPolicy getEmailCommunicationPolicy();
}
