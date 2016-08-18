package org.apereo.cas.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proxy validation controller.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Controller("proxyValidateController")
public class ProxyValidateController extends AbstractServiceValidateController {
    
    /**
     * Handle model and view.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path="/proxyValidate", method = RequestMethod.GET)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, 
                                                 final HttpServletResponse response)
        throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
