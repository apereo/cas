package org.apereo.cas.support.pac4j.logout;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.logout.handler.LogoutHandler;

/**
 * pac4j logout handler specific to the CAS server.
 *
 * @author Jerome Leleu
 * @since 5.3.6
 */
public class CasServerSpecificLogoutHandler implements LogoutHandler<J2EContext> {

    @Override
    public void recordSession(final J2EContext context, final String key) {
    }

    @Override
    public void destroySessionFront(final J2EContext context, final String key) {
        throw new RequestSloException(key, true);
    }

    @Override
    public void destroySessionBack(final J2EContext context, final String key) {
        throw new RequestSloException(key, false);
    }

    @Override
    public void renewSession(final String oldSessionId, final J2EContext context) {
    }
}
