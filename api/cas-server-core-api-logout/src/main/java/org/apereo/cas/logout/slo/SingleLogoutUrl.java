package org.apereo.cas.logout.slo;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Additional settings relevant for the logout url.
     */
    private final Map<String, String> properties = new LinkedHashMap<>(2);

    /**
     * Determine logout url assigned to a registered service.
     *
     * @param registeredService the registered service
     * @return the list
     */
    public static List<SingleLogoutUrl> from(final RegisteredService registeredService) {
        if (registeredService != null && StringUtils.hasText(registeredService.getLogoutUrl())) {
            return Arrays.stream(StringUtils.commaDelimitedListToStringArray(registeredService.getLogoutUrl()))
                .map(url -> new SingleLogoutUrl(url, registeredService.getLogoutType()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }
}
