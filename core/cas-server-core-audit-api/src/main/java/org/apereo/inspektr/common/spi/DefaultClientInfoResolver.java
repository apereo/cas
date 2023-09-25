package org.apereo.inspektr.common.spi;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;

/**
 * Default implementation that gets it from the {@link ThreadLocal}.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@Slf4j
public class DefaultClientInfoResolver implements ClientInfoResolver {
    @Override
    public ClientInfo resolveFrom(final JoinPoint joinPoint, final Object retVal) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null) {
            return resolveClientInfo(clientInfo);
        }
        LOGGER.warn("No ClientInfo could be found. Returning empty ClientInfo object.");
        return ClientInfo.empty();
    }

    protected ClientInfo resolveClientInfo(final ClientInfo clientInfo) {
        return clientInfo;
    }
}
