package org.apereo.cas.logout.slo;

import org.apereo.cas.services.RegisteredServiceLogoutType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * This is {@link SingleLogoutUrl}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class SingleLogoutUrl implements Serializable {
    private static final long serialVersionUID = 6611608175787696823L;
    /**
     * The URL to the logout endpoint where the logout message will be sent.
     */
    private final String url;

    /**
     * The http-logoutType or binding that should be used to send the message to the url.
     */
    private final RegisteredServiceLogoutType logoutType;
}
