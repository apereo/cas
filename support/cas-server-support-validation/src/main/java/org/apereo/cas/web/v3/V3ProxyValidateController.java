package org.apereo.cas.web.v3;

import org.apereo.cas.CasProtocolConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
public class V3ProxyValidateController extends V3ServiceValidateController {

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3)
    @Override
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
