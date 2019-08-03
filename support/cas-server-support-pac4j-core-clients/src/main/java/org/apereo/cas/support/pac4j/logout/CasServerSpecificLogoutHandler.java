package org.apereo.cas.support.pac4j.logout;

import org.pac4j.core.context.JEEContext;
import org.pac4j.core.logout.handler.LogoutHandler;

/**
 * pac4j logout handler specific to the CAS server.
 *
 * @author Jerome Leleu
 * @since 5.3.6
 */
public class CasServerSpecificLogoutHandler implements LogoutHandler<JEEContext> {

    @Override
    public void recordSession(final JEEContext context, final String key) {
    }

    @Override
    public void destroySessionFront(final JEEContext context, final String key) {
        throw new RequestSloException(key, true);
    }

    @Override
    public void destroySessionBack(final JEEContext context, final String key) {
        throw new RequestSloException(key, false);
    }

    @Override
    public void renewSession(final String oldSessionId, final JEEContext context) {
    }
}
