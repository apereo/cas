package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link CasModelRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface CasModelRegisteredService extends WebBasedRegisteredService {
    /**
     * Get the proxy policy rules for this service.
     *
     * @return the proxy policy
     */
    RegisteredServiceProxyPolicy getProxyPolicy();

    /**
     * Gets proxy granting ticket expiration policy.
     *
     * @return the proxy granting ticket expiration policy
     */
    RegisteredServiceProxyGrantingTicketExpirationPolicy getProxyGrantingTicketExpirationPolicy();

    /**
     * Gets service ticket expiration policy.
     *
     * @return the service ticket expiration policy
     */
    RegisteredServiceServiceTicketExpirationPolicy getServiceTicketExpirationPolicy();

    /**
     * Gets proxy ticket expiration policy.
     *
     * @return the proxy ticket expiration policy
     */
    RegisteredServiceProxyTicketExpirationPolicy getProxyTicketExpirationPolicy();

    /**
     * Identifies the redirect url that will be used
     * when building a response to authentication requests.
     * The url is ultimately used to carry the service ticket
     * back to the application and will override the default
     * url which is tracked by the {@link WebApplicationService#getOriginalUrl()}.
     *
     * @return the redirect url for this service
     * @since 6.2
     */
    String getRedirectUrl();

    /**
     * Indicates the collection of CAS protocol versions that this
     * application should allow and support.
     *
     * @return collection of supported protocol versions.
     */
    Set<CasProtocolVersions> getSupportedProtocols();

    /**
     * Response determines how CAS should contact the matching service
     * typically with a ticket id. By default, the strategy is a 302 redirect.
     *
     * @return the response type
     * @see Response.ResponseType
     */
    String getResponseType();
}
