package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.saml.SamlIdPConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

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
}
