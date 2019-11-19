package org.apereo.cas.support.oauth.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.validation.ValidationResponseType;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link OAuthApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Travis Schmidt
 * @since 6.1
 */
@Slf4j
public class OAuthApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {

    /**
     * Determine web application format boolean.
     *
     * @param request               the request
     * @param webApplicationService the web application service
     * @return the service itself.
     */
    private static AbstractWebApplicationService determineWebApplicationFormat(final HttpServletRequest request,
                                                                               final AbstractWebApplicationService webApplicationService) {
        val format = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_FORMAT) : null;
        try {
            if (StringUtils.isNotBlank(format)) {
                val formatType = ValidationResponseType.valueOf(format.toUpperCase());
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
     * @param clientId    the client id
     * @param serviceToUse the service to use
     * @return the simple web application service
     */
    protected static AbstractWebApplicationService newWebApplicationService(final HttpServletRequest request,
                                                                            final String clientId,
                                                                            final String serviceToUse) {
        val artifactId = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_TICKET) : null;
        val id = StringUtils.isNotBlank(clientId) ? clientId : extractClientId(serviceToUse);
        val newService = new OAuthWebApplicationService(id, serviceToUse, artifactId);
        determineWebApplicationFormat(request, newService);
        val source = getSourceParameter(request, CasProtocolConstants.PARAMETER_TARGET_SERVICE,
            CasProtocolConstants.PARAMETER_SERVICE);
        newService.setSource(source);
        return newService;
    }


    /**
     * Gets requested service.
     *
     * @param request the request
     * @return the requested service
     */
    protected String getClientId(final HttpServletRequest request) {
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        if (StringUtils.isNotBlank(clientId)) {
            return clientId;
        }
        return null;
    }

    private static String extractClientId(final String param) {
        if (StringUtils.isBlank(param) || !param.contains(OAuth20Constants.CLIENT_ID)) {
            return null;
        }
        val start = param.indexOf(OAuth20Constants.CLIENT_ID + "=") + OAuth20Constants.CLIENT_ID.length() + 1;
        val end = param.indexOf("&", start);
        val id = param.substring(start, end);
        return id;
    }

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        val serviceParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        val clientId = request.getParameterMap().containsKey(OAuth20Constants.CLIENT_ID)
                ? getClientId(request)
                : extractClientId(serviceParam);
        if (StringUtils.isBlank(clientId)) {
            LOGGER.trace("No service is specified in the request. Skipping service creation");
            return null;
        }
        return newWebApplicationService(request, clientId, serviceParam);
    }

    @Override
    public WebApplicationService createService(final String id) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val clientId = extractClientId(id);
        return newWebApplicationService(request, clientId, id);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
