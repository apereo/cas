package org.apereo.cas.support.pac4j.logout;

import lombok.experimental.UtilityClass;
import org.pac4j.core.logout.handler.LogoutHandler;

/**
 * The pac4j logout handler which does nothing.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@UtilityClass
public class NoOpLogoutHandler implements LogoutHandler {
    /**
     * The class instance.
     */
    public static final LogoutHandler INSTANCE = new NoOpLogoutHandler();
}
