package org.jasig.cas.authentication.principal;

import org.jasig.cas.validation.ValidationResponseType;

/**
 * Represents a service using CAS that comes from the web.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface WebApplicationService extends Service {

    /**
     * Constructs the url to redirect the service back to.
     *
     * @param ticketId the service ticket to provide to the service.
     * @return the redirect url.
     */
    Response getResponse(String ticketId);

    /**
     * Retrieves the artifact supplied with the service. May be null.
     *
     * @return the artifact if it exists, null otherwise.
     */
    String getArtifactId();

    /**
     * Return the original url provided (as {@code service} or {@code targetService} request parameter).
     * Used to reconstruct the redirect url.
     *
     * @return the original url provided.
     */
    String getOriginalUrl();

    /**
     *  Ticket validation response MUST be produced based on the parameter value.
     *  Supported values are XML and JSON. If this parameter is not set,
     *  the default XML format will be used. If the parameter value is not supported by the CAS server,
     *  an error must be produced.
     * @return the requested format
     * @since 4.2
     */
    ValidationResponseType getFormat();
}
