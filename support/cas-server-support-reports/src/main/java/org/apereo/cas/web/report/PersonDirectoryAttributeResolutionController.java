package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link PersonDirectoryAttributeResolutionController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("PersonDirectoryAttributeResolutionController")
@RequestMapping("/status/attrresolution")
public class PersonDirectoryAttributeResolutionController {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView("monitoring/attrresolution");
    }

}
