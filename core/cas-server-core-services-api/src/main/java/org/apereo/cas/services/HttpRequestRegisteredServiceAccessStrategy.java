package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import java.io.Serial;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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

    @Serial
    private static final long serialVersionUID = -1108201604115278440L;

    private String ipAddress;

    private String userAgent;

    @JsonProperty("headers")
    private Map<String, String> headers = new TreeMap<>();

    @Override
    public boolean isServiceAccessAllowed(final RegisteredService registeredService, final Service service) {
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
                if (match && !headers.isEmpty()) {
                    LOGGER.debug("Evaluating request headers [{}] against pattern [{}]",
                        info.getHeaders(), this.headers);
                    match = headers.entrySet()
                        .stream()
                        .filter(header -> info.getHeaders().containsKey(header.getKey()))
                        .allMatch(header -> {
                            val headerValue = info.getHeaders().get(header.getKey());
                            return RegexUtils.find(header.getValue(), headerValue.toString());
                        });
                }
                return match;
            });
    }
}
