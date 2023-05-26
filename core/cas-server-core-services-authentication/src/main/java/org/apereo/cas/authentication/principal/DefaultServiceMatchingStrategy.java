package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * This is {@link DefaultServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultServiceMatchingStrategy implements ServiceMatchingStrategy {
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("#.+");

    private final ServicesManager servicesManager;

    @Override
    public boolean matches(final Service service, final Service serviceToMatch) {
        /*
        service - the original service supplied when the ticket was created.
        serviceToMatch - the provided service requesting the ticket to be validated.
        */
        try {
            val thisUrl = removeFragmentFrom(URLDecoder.decode(service.getId(), StandardCharsets.UTF_8));
            val serviceUrl = removeFragmentFrom(URLDecoder.decode(serviceToMatch.getId(), StandardCharsets.UTF_8));
            LOGGER.debug("Decoded urls and comparing [{}] with [{}]", thisUrl, serviceUrl);
            return thisUrl.equalsIgnoreCase(serviceUrl);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    private static String removeFragmentFrom(final String id) {
        return FRAGMENT_PATTERN.matcher(id).replaceAll(StringUtils.EMPTY);
    }
}
