package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import java.util.function.Supplier;

/**
 * This is {@link IpAddressAuthorizationManager}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class IpAddressAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final CasConfigurationProperties casProperties;

    private final ActuatorEndpointProperties properties;

    @Override
    public AuthorizationDecision check(final Supplier authentication, final RequestAuthorizationContext context) {
        val remoteAddr = StringUtils.defaultIfBlank(
            context.getRequest().getHeader(casProperties.getAudit().getEngine().getAlternateClientAddrHeaderName()),
            context.getRequest().getRemoteAddr());

        val granted = properties.getRequiredIpAddresses()
            .stream()
            .anyMatch(pattern -> RegexUtils.matchesIpAddress(pattern, remoteAddr));
        if (!granted) {
            LOGGER.warn("Provided regular expression or IP/netmask [{}] does not match [{}]",
                properties.getRequiredIpAddresses(), remoteAddr);
        }
        return new AuthorizationDecision(granted);

    }
}
