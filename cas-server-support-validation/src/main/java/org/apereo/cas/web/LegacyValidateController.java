package org.apereo.cas.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
@Controller("legacyValidateController")
public class LegacyValidateController extends AbstractServiceValidateController {

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path = "/validate", method = RequestMethod.GET)
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
