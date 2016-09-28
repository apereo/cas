package org.apereo.cas.web.flow;

import com.google.common.collect.Maps;
import org.apereo.cas.CasProtocolConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link Pac4jErrorViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class Pac4jErrorViewResolver implements ErrorViewResolver {

    @Autowired
    @Qualifier("conventionErrorViewResolver")
    private ErrorViewResolver conventionErrorViewResolver;
    
    @Override
    public ModelAndView resolveErrorView(final HttpServletRequest request,
                                         final HttpStatus status, final Map<String, Object> map) {

        final Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey("error") && params.containsKey("error_code") && params.containsKey("error_description")) {
            final Map<String, Object> model = Maps.newHashMap();
            model.put("code", status.value());
            model.put("error", request.getParameter("error"));
            model.put("reason", request.getParameter("error_reason"));
            model.put("description", request.getParameter("error_description"));
            model.put("service", request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
            model.put("client", request.getParameter("client_name"));
            model.putAll(map);
            return new ModelAndView("casPac4jStopWebflow", model);
        }
        return conventionErrorViewResolver.resolveErrorView(request, status, map);
    }
}
