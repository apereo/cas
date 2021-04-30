package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import java.util.Set;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebApplicationServletInitializer extends SpringBootServletInitializer {

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        servletContext.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        super.onStartup(servletContext);
    }
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
            .sources(CasWebApplication.class)
            .banner(CasEmbeddedContainerUtils.getCasBannerInstance());
    }
}
