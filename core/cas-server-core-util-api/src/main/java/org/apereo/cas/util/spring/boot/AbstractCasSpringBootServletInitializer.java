package org.apereo.cas.util.spring.boot;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.metrics.ApplicationStartup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import java.util.List;
import java.util.Set;

/**
 * This is {@link AbstractCasSpringBootServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public abstract class AbstractCasSpringBootServletInitializer extends SpringBootServletInitializer {
    private final List<Class<?>> sources;

    private final Banner banner;

    private final ApplicationStartup applicationStartup;

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        servletContext.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        super.onStartup(servletContext);
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
            .sources(sources.toArray(Class[]::new))
            .applicationStartup(this.applicationStartup)
            .logStartupInfo(true)
            .banner(this.banner);
    }

}
