package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultServiceMatchingStrategy implements ServiceMatchingStrategy {
    private static final Pattern FRAGMENT_PATTERN = RegexUtils.createPattern("#.+");

    protected final ServicesManager servicesManager;

    @Override
    public boolean matches(@Nullable final Service service, @Nullable final Service serviceToMatch) {
        try {
            if (service != null && serviceToMatch != null) {
                return compareServices(service, serviceToMatch);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Compare the service urls, ignoring case and fragments. Everything before the query is compared as sent, so that
     * an encoded character, such as an encoded fragment in the authority, never makes two different urls match.
     * The query is compared once decoded, so that a parameter encoded differently by the application and by the
     * client that validates the ticket, such as a space sent as {@code +} or as {@code %20}, still matches.
     *
     * @param service        the service
     * @param serviceToMatch the service to match
     * @return true if the services match
     * @throws Exception the exception
     */
    protected boolean compareServices(final Service service, final Service serviceToMatch) throws Exception {
        val thisUrl = decodeQueryOf(removeFragmentFrom(service.getId()));
        val serviceUrl = decodeQueryOf(removeFragmentFrom(serviceToMatch.getId()));
        LOGGER.debug("Comparing service urls [{}] with [{}]", thisUrl, serviceUrl);
        return thisUrl.equalsIgnoreCase(serviceUrl);
    }

    private static String removeFragmentFrom(final String id) {
        return FRAGMENT_PATTERN.matcher(id).replaceAll(StringUtils.EMPTY);
    }

    private static String decodeQueryOf(final String url) {
        val queryStart = url.indexOf('?');
        return queryStart < 0
            ? url
            : url.substring(0, queryStart + 1) + URLDecoder.decode(url.substring(queryStart + 1), StandardCharsets.UTF_8);
    }
}
