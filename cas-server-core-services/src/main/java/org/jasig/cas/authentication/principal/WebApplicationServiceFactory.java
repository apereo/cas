package org.jasig.cas.authentication.principal;

import org.jasig.cas.CasProtocolConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link WebApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("webApplicationServiceFactory")
public class WebApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        final String targetService = request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE);
        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        final String serviceAttribute = (String) request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
        final String method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        final String serviceToUse;
        if (StringUtils.hasText(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.hasText(service)) {
            serviceToUse = service;
        } else {
            serviceToUse = serviceAttribute;
        }

        if (!StringUtils.hasText(serviceToUse)) {
            return null;
        }

        final String id = AbstractServiceFactory.cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);

        final  Response.ResponseType type = "POST".equalsIgnoreCase(method) ? Response.ResponseType.POST
                                                        : Response.ResponseType.REDIRECT;

        final WebApplicationService webApplicationService = new SimpleWebApplicationServiceImpl(id, serviceToUse,
                artifactId, new WebApplicationServiceResponseBuilder(type));
        return webApplicationService;
    }

    @Override
    public WebApplicationService createService(final String id) {
        return new SimpleWebApplicationServiceImpl(id, id, null,
                new WebApplicationServiceResponseBuilder(Response.ResponseType.REDIRECT));
    }
}
