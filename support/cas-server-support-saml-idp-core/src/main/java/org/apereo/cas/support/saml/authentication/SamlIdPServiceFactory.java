package org.apereo.cas.support.saml.authentication;


import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link SamlIdPServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlIdPServiceFactory extends WebApplicationServiceFactory {
    @Override
    protected String getRequestedService(final HttpServletRequest request) {
        val providerId = request.getParameter(SamlIdPConstants.PROVIDER_ID);
        if (StringUtils.isNotBlank(providerId)) {
            LOGGER.trace("Extracted provider id from the request is [{}]", providerId);
            return providerId;
        }
        return super.getRequestedService(request);
    }

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        val serviceToUse = getRequestedService(request);
        if (StringUtils.isBlank(serviceToUse)) {
            LOGGER.trace("No service is specified in the request. Skipping service creation");
            return null;
        }
        return newWebApplicationService(request, serviceToUse);
    }

    @Override
    public WebApplicationService createService(final String id) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        return newWebApplicationService(request, id);
    }

    protected static AbstractWebApplicationService newWebApplicationService(final HttpServletRequest request,
                                                                            final String serviceToUse) {
        val artifactId = Optional.ofNullable(request)
                .map(httpServletRequest -> httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_TICKET))
                .orElse(null);
        val id = cleanupUrl(serviceToUse);
        val newService = new SamlService();
        newService.setId(id);
        newService.setArtifactId(artifactId);
        newService.setOriginalUrl(serviceToUse);
        determineWebApplicationFormat(request, newService);
        val source = getSourceParameter(request, CasProtocolConstants.PARAMETER_TARGET_SERVICE,
                CasProtocolConstants.PARAMETER_SERVICE);
        newService.setSource(source);
        return newService;
    }

}
