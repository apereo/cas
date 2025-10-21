package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * The release policy condition controls whether the policy should be activated and support a request.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAttributeReleaseActivationCriteria extends Serializable {

    /**
     * Is authorized to release authentication attributes.
     *
     * @return true/false
     */
    boolean shouldActivate(RegisteredServiceAttributeReleasePolicyContext context);
}
