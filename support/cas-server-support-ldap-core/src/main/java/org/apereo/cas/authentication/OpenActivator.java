package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.ldaptive.Connection;
import org.ldaptive.pool.Activator;

/**
 * This component attempts to re-open and activate a connection
 * if it's defined and not open. This is generally used when
 * configuring LDAP with {@link org.ldaptive.pool.ClosePassivator}.
 * <p>
 * Note: {@link org.ldaptive.pool.ConnectActivator} does not check whether the connection is already open,
 * which can cause an exception when opening an open connection.
 *
 * @author smaszno
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OpenActivator implements Activator<Connection> {

    @Override
    public boolean activate(final Connection c) {
        try {
            if (c == null) {
                return false;
            }
            if (c.isOpen()) {
                return true;
            }
            c.open();
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
