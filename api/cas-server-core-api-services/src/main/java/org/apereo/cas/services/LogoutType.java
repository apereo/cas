package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Enumeration of the logout type.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY)
public enum LogoutType {
    /**
     * For no SLO.
     */
    NONE,
    /**
     * For back channel SLO.
     */
    BACK_CHANNEL,
    /**
     * For front channel SLO.
     */
    FRONT_CHANNEL
}
