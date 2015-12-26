package org.jasig.cas.web.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at {@code /status/config}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller("internalConfigController")
public final class InternalConfigStateController {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = true)
    @Qualifier("casProperties")
    private Properties casProperties;

    /**
     * Handle request.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET, value="/status/config")
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        return new ModelAndView(VIEW_CONFIG);
    }

    /**
     * Returns the current state of CAS properties.
     * @return properties configured in CAS.
     */
    @RequestMapping(value = "/getProperties", method = RequestMethod.GET)
    @ResponseBody
    protected Set<Map.Entry<Object, Object>> getProperties() {
        return casProperties.entrySet();
    }

}
