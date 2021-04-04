package org.apereo.cas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import java.util.Set;

/**
 * This is {@link CasSpringBootAdminServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasSpringBootAdminServletInitializer extends SpringBootServletInitializer {

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        servletContext.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        super.onStartup(servletContext);
    }
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
            .sources(CasSpringBootAdminServerWebApplication.class)
            .banner(new CasSpringBootAdminServerBanner())
            .logStartupInfo(true);
    }
}
