package org.apereo.cas.services;

import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import java.util.Optional;

/**
 * This is {@link HttpRequestRegisteredServiceAccessStrategy} that reaches out
 * to a remote endpoint, passing the CAS principal id to determine if access is allowed.
 * If the status code returned in the final response is not accepted by the policy here,
 * access shall be denied.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class HttpRequestRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -1108201604115278440L;

    private String ipAddress;

    private String userAgent;

    @Override
    public boolean isServiceAccessAllowed() {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo())
            .stream()
            .anyMatch(info -> {
                var match = true;
                if (StringUtils.isNoneBlank(this.ipAddress)) {
                    LOGGER.debug("Evaluating IP address [{}] against pattern [{}]",
                        info.getClientIpAddress(), this.ipAddress);
                    match = RegexUtils.find(this.ipAddress, info.getClientIpAddress());
                }
                if (match && StringUtils.isNoneBlank(this.userAgent)) {
                    LOGGER.debug("Evaluating user agent [{}] against pattern [{}]",
                        info.getUserAgent(), this.userAgent);
                    match = RegexUtils.find(this.userAgent, info.getUserAgent());
                }
                return match;
            });
    }
}
