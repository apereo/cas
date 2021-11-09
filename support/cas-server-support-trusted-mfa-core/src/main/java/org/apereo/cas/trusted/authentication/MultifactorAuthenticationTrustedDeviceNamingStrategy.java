package org.apereo.cas.trusted.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * This is {@link MultifactorAuthenticationTrustedDeviceNamingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrustedDeviceNamingStrategy {

    /**
     * Generate device name based on request IP and date/time.
     *
     * @return the multifactor authentication trusted device naming strategy
     */
    static MultifactorAuthenticationTrustedDeviceNamingStrategy random() {
        return (registeredService, service, request, authentication) -> {
            val builder = new StringBuilder();
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (clientInfo != null) {
                builder.append(clientInfo.getClientIpAddress());
                builder.append('@');
            }
            builder.append(LocalDateTime.now(ZoneOffset.UTC));
            return builder.toString();
        };
    }

    /**
     * Determine device name.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @param request           the request
     * @param authentication    the authentication
     * @return the string
     */
    String determineDeviceName(RegisteredService registeredService,
                               Service service,
                               HttpServletRequest request,
                               Authentication authentication);
}
