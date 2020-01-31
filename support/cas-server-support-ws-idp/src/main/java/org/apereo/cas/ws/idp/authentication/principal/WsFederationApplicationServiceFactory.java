package org.apereo.cas.ws.idp.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.validation.ValidationResponseType;
import org.apereo.cas.ws.idp.WSFederationConstants;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link WsFederationApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Slf4j
public class WsFederationApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {

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
     * @param replyId    the client id
     * @param serviceToUse the service to use
     * @return the simple web application service
     */
    protected static AbstractWebApplicationService newWebApplicationService(final HttpServletRequest request,
                                                                            final String replyId,
                                                                            final String serviceToUse) {
        val artifactId = request != null ? request.getParameter(CasProtocolConstants.PARAMETER_TICKET) : null;
        val id = StringUtils.isNotBlank(replyId) ? replyId : extractReplyId(serviceToUse);
        val newService = new WsFederationWebApplicationService(id, serviceToUse, artifactId);
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
    protected String getReplyId(final HttpServletRequest request) {
        val entityId = request.getParameter(WSFederationConstants.WREPLY);
        if (StringUtils.isNotBlank(entityId)) {
            return entityId;
        }
        return null;
    }

    private static String extractReplyId(final String param) {
        if (StringUtils.isBlank(param) || !param.contains(WSFederationConstants.WREPLY)) {
            return null;
        }
        val start = param.indexOf(WSFederationConstants.WREPLY + "=") + WSFederationConstants.WREPLY.length() + 1;
        val end = param.indexOf("&", start);
        val id = EncodingUtils.urlDecode(param.substring(start, end));
        return id;
    }

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        val serviceParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        val entityId = request.getParameterMap().containsKey(WSFederationConstants.WREPLY)
                ? getReplyId(request)
                : extractReplyId(serviceParam);
        if (StringUtils.isBlank(entityId)) {
            LOGGER.trace("No service is specified in the request. Skipping service creation");
            return null;
        }
        return newWebApplicationService(request, entityId, serviceParam);
    }

    @Override
    public WebApplicationService createService(final String id) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        return newWebApplicationService(request, id, id);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
