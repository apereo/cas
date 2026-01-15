package org.apereo.cas.gua.api;

import module java.base;
import com.google.common.io.ByteSource;

/**
 * This is {@link UserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface UserGraphicalAuthenticationRepository extends Serializable {

    /**
     * Gets graphics.
     *
     * @param username the username
     * @return the graphics
     */
    ByteSource getGraphics(String username);
}
