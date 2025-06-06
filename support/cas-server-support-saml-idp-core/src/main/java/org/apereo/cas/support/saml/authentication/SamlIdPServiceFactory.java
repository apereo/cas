package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.web.UrlValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link SamlIdPServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class SamlIdPServiceFactory extends WebApplicationServiceFactory {
    public SamlIdPServiceFactory(final TenantExtractor tenantExtractor,
                                 final UrlValidator urlValidator) {
        super(tenantExtractor, urlValidator);
    }

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
