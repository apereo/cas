package org.apereo.cas.mgmt.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link CasManagementRootController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasManagementRootController extends ParameterizableViewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasManagementRootController.class);
    
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        final String url = request.getContextPath() + "/manage.html";
        LOGGER.debug("Initial url is [{}]", url);
        
        final String encodedUrl = response.encodeURL(url);
        LOGGER.debug("Encoded url is [{}]", encodedUrl);

        return new ModelAndView(new RedirectView(encodedUrl));
    }
}
