package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.validation.ValidationResponseType;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link WebApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {

    /**
     * Determine response type response.
     *
     * @param request the request
     * @return the response . response type
     */
    protected Response.ResponseType determineResponseType(final HttpServletRequest request) {
        final String method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        return HttpMethod.POST.name().equalsIgnoreCase(method) ? Response.ResponseType.POST : Response.ResponseType.REDIRECT;
    }

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        final String serviceToUse = getRequestedService(request);
        if (StringUtils.isBlank(serviceToUse)) {
            logger.debug("No service is specified in the request. Skipping service creation");
            return null;
        }

        final Response.ResponseType type = determineResponseType(request);
        return newWebApplicationService(request, serviceToUse, type);
    }

    /**
     * Determine web application format boolean.
     *
     * @param request               the request
     * @param webApplicationService the web application service
     * @return the service itself.
     */
    private AbstractWebApplicationService determineWebApplicationFormat(final HttpServletRequest request,
                                                                        final AbstractWebApplicationService webApplicationService) {
        final String format = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_FORMAT) : null;
        try {
            if (StringUtils.isNotBlank(format)) {
                final ValidationResponseType formatType = ValidationResponseType.valueOf(format.toUpperCase());
                webApplicationService.setFormat(formatType);
            }
        } catch (final Exception e) {
            logger.error("Format specified in the request [{}] is not recognized", format);
        }
        return webApplicationService;
    }

    /**
     * Build new web application service simple web application service.
     *
     * @param request      the request
     * @param serviceToUse the service to use
     * @param type         the type
     * @return the simple web application service
     */
    protected AbstractWebApplicationService newWebApplicationService(final HttpServletRequest request,
                                                                     final String serviceToUse,
                                                                     final Response.ResponseType type) {
        final String artifactId = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_TICKET) : null;
        final String id = cleanupUrl(serviceToUse);
        final AbstractWebApplicationService newService = new SimpleWebApplicationServiceImpl(id, serviceToUse,
                artifactId, newWebApplicationServiceResponseBuilder(type));
        determineWebApplicationFormat(request, newService);
        return newService;
    }


    /**
     * New web application service response builder.
     *
     * @param type the type
     * @return the response builder
     */
    protected ResponseBuilder<WebApplicationService> newWebApplicationServiceResponseBuilder(final Response.ResponseType type) {
        return new WebApplicationServiceResponseBuilder(type);
    }

    /**
     * Gets requested service.
     *
     * @param request the request
     * @return the requested service
     */
    protected String getRequestedService(final HttpServletRequest request) {
        final String targetService = request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE);
        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        final Object serviceAttribute = request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);

        String serviceToUse = null;
        if (StringUtils.isNotBlank(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.isNotBlank(service)) {
            serviceToUse = service;
        } else if (serviceAttribute != null) {
            if (serviceAttribute instanceof Service) {
                serviceToUse = ((Service) serviceAttribute).getId();
            } else {
                serviceToUse = serviceAttribute.toString();
            }
        }

        if (StringUtils.isBlank(serviceToUse)) {
            return null;
        }
        return serviceToUse;
    }

    @Override
    public WebApplicationService createService(final String id) {
        return newWebApplicationService(null, id, Response.ResponseType.REDIRECT);
    }
}
