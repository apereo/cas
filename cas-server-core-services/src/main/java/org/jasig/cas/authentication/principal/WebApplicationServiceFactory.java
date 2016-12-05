package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.validation.ValidationResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        final String targetService = request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE);
        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        final String serviceAttribute = (String) request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);
        final String method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        final String format = request.getParameter(CasProtocolConstants.PARAMETER_FORMAT);

        String serviceToUse;
        if (StringUtils.isNotBlank(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.isNotBlank(service)) {
            serviceToUse = service;
        } else {
            serviceToUse = serviceAttribute;
        }

        if (StringUtils.isBlank(serviceToUse)) {
            return null;
        }

        //To use in dev server, modify a serviceToUse's protocol from https to http
        //Because components communicate with each other with http, not https
        //Differentiate service id and originalUrl
        //Because the originaUrl will be used for "Location" value of http status 302
        //The value is for sending to a user browser, not for internal components
        final String originalUrl = serviceToUse;
        try {
            final URL url = new URL(serviceToUse);
            if(url.getProtocol().equalsIgnoreCase("https")) {
                final StringBuilder builder = new StringBuilder();
                builder
                    .append("http")
                    .append("://")
                    .append(url.getHost());
                if(url.getPort() != -1) {
                    builder.append(":").append(url.getPort());
                }
                builder.append(url.getFile());
                serviceToUse = builder.toString();
            }
        } catch (final MalformedURLException e) {
            logger.error("Service URL String is not converted to URL: {}", e.toString());
        }

        final String id = AbstractServiceFactory.cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);

        final Response.ResponseType type = HttpMethod.POST.name().equalsIgnoreCase(method) ? Response.ResponseType.POST
                : Response.ResponseType.REDIRECT;

        final SimpleWebApplicationServiceImpl webApplicationService =
                new SimpleWebApplicationServiceImpl(id, originalUrl,
                        artifactId, new WebApplicationServiceResponseBuilder(type));

        try {
            if (StringUtils.isNotBlank(format)) {
                final ValidationResponseType formatType = ValidationResponseType.valueOf(format.toUpperCase());
                webApplicationService.setFormat(formatType);
            }
        } catch (final Exception e) {
            logger.error("Format specified in the request [{}] is not recognized", format);
            return null;
        }
        return webApplicationService;
    }

    @Override
    public WebApplicationService createService(final String id) {
        return new SimpleWebApplicationServiceImpl(id, id, null,
                new WebApplicationServiceResponseBuilder(Response.ResponseType.REDIRECT));
    }
}
