package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * Represents the task of building a CAS response
 * that is returned by a service.
 *
 * @param <T> the type parameter
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ResponseBuilder<T extends WebApplicationService> extends Serializable, Ordered {

    /**
     * Build response. The implementation must produce
     * a response that is based on the type passed. Given
     * the protocol used, the ticket id may be passed
     * as part of the response. If the response type
     * is not recognized, an error must be thrown back.
     *
     * @param service         the service
     * @param serviceTicketId the service ticket id
     * @param authentication  the authentication
     * @return the response
     */
    Response build(T service, String serviceTicketId, Authentication authentication);

    /**
     * Supports this service.
     *
     * @param service the service
     * @return true /false
     */
    default boolean supports(final T service) {
        return service != null;
    }
}
