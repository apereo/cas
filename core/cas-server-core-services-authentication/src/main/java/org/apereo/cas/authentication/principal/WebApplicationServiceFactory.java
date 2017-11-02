package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.validation.ValidationResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link WebApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationServiceFactory.class);


    /**
     * Determine web application format boolean.
     *
     * @param request               the request
     * @param webApplicationService the web application service
     * @return the service itself.
     */
    private static AbstractWebApplicationService determineWebApplicationFormat(final HttpServletRequest request,
                                                                               final AbstractWebApplicationService webApplicationService) {
        final String format = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_FORMAT) : null;
        try {
            if (StringUtils.isNotBlank(format)) {
                final ValidationResponseType formatType = ValidationResponseType.valueOf(format.toUpperCase());
                webApplicationService.setFormat(formatType);
            }
        } catch (final Exception e) {
            LOGGER.error("Format specified in the request [{}] is not recognized", format);
        }
        return webApplicationService;
    }

    /**
     * Build new web application service simple web application service.
     *
     * @param request      the request
     * @param serviceToUse the service to use
     * @return the simple web application service
     */
    protected static AbstractWebApplicationService newWebApplicationService(final HttpServletRequest request,
                                                                            final String serviceToUse) {
        final String artifactId = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_TICKET) : null;
        final String id = cleanupUrl(serviceToUse);
        final AbstractWebApplicationService newService = new SimpleWebApplicationServiceImpl(id, serviceToUse, artifactId);
        determineWebApplicationFormat(request, newService);
        return newService;
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
    public WebApplicationService createService(final HttpServletRequest request) {
        final String serviceToUse = getRequestedService(request);
        if (StringUtils.isBlank(serviceToUse)) {
            LOGGER.debug("No service is specified in the request. Skipping service creation");
            return null;
        }
        return newWebApplicationService(request, serviceToUse);
    }

    @Override
    public WebApplicationService createService(final String id) {
        return newWebApplicationService(HttpRequestUtils.getHttpServletRequestFromRequestAttributes(), id);
    }
}
