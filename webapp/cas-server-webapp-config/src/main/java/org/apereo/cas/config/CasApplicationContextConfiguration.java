package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link CasApplicationContextConfiguration}.that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casApplicationContextConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasApplicationContextConfiguration {

    @Bean
    protected UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }

    @Bean
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
                final String queryString = request.getQueryString();
                final String url = request.getContextPath() + "/login" + (queryString != null ? '?' + queryString : StringUtils.EMPTY);
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }
        };
    }
}
