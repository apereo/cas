package org.apereo.cas.services;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link OidcBackchannelTokenDeliveryModes}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@RequiredArgsConstructor
public enum OidcBackchannelTokenDeliveryModes {
    /**
     * Push oidc backchannel token delivery modes.
     */
    PING("ping"),
    /**
     * Push oidc backchannel token delivery modes.
     */
    PUSH("push"),
    /**
     * Poll oidc backchannel token delivery modes.
     */
    POLL("poll");

    private final String mode;
}
