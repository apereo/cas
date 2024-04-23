package org.apereo.cas.sba;

import org.apereo.cas.util.app.ApplicationEntrypointInitializer;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import java.util.List;

/**
 * This is {@link CasSpringBootAdminServerInitializer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasSpringBootAdminServerInitializer implements ApplicationEntrypointInitializer {
    @Override
    public List<Class> getApplicationSources(final String[] args) {
        return List.of(CasSpringBootAdminServer.class);
    }

    @EnableAdminServer
    static class CasSpringBootAdminServer {
    }
}
