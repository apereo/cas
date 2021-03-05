package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAcceptableUsagePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAcceptableUsagePolicy extends Serializable {

    /**
     * Indicate whether policy is enabled.
     *
     * @return true/false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Gets message code that links
     * the policy terms and body
     * to a language bundle code..
     *
     * @return the message code
     */
    default String getMessageCode() {
        return null;
    }

    /**
     * Gets the policy text verbatim..
     *
     * @return the text
     */
    default String getText() {
        return null;
    }
}
