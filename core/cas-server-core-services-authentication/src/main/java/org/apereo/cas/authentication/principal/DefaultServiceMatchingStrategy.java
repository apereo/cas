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

    protected boolean compareServices(final Service service, final Service serviceToMatch) throws Exception {
        val thisUrl = removeFragmentFrom(URLDecoder.decode(service.getId(), StandardCharsets.UTF_8));
        val serviceUrl = removeFragmentFrom(URLDecoder.decode(serviceToMatch.getId(), StandardCharsets.UTF_8));
        LOGGER.debug("Decoded urls and comparing [{}] with [{}]", thisUrl, serviceUrl);
        return thisUrl.equalsIgnoreCase(serviceUrl);
    }

    private static String removeFragmentFrom(final String id) {
        return FRAGMENT_PATTERN.matcher(id).replaceAll(StringUtils.EMPTY);
    }
}
