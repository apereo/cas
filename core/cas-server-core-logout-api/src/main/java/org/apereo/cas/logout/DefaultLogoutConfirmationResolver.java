package org.apereo.cas.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultLogoutConfirmationResolver}.
 * Logout requests are confirmed if logout confirmation flag is
 * enabled in the CAS properties and the request context
 * indicates that the logout request is confirmed or if
 * we are dealing with a (logout) SAML response, such as cases that deal with logout responses
 * from external identity providers and delegation.
 * If the confirmation flag is disabled, all logout requests
 * are considered confirmed anyway.
 * Note that the SAML response check is somewhat naive in directly checking for the presence of
 * {@code SAMLResponse} in the request parameters. Future versions of CAS would consider
 * turning this check into a proper bean that is moved to the right module.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DefaultLogoutConfirmationResolver implements LogoutConfirmationResolver {
    private final CasConfigurationProperties casProperties;

    @Override
    public boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        if (casProperties.getLogout().isConfirmLogout()) {
            return WebUtils.isLogoutRequestConfirmed(requestContext)
                || requestContext.getRequestParameters().contains("SAMLResponse");
        }
        return true;
    }
}
